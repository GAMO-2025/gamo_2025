package gamo.web.videocall.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gamo.web.videocall.domain.CallType;
import gamo.web.videocall.dto.Message;
import gamo.web.videocall.service.CallManager;
import gamo.web.videocall.service.VideoCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignalHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CallManager callStatusManager;
    private final VideoCallService videoCallService;

    // 현재 연결된 WebSocket 세션
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 클라이언트 연결 성공 시
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserId(session);
        sessions.put(userId, session);

        callStatusManager.cancelDisconnectTimeout(userId);

        log.info("Connected: {}", userId);
    }
    // 메시지 수신 처리
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode json = objectMapper.readTree(message.getPayload());
        String type = json.get("type").asText();
        JsonNode payload = json.get("payload");

        switch (type) {
            case "CALL_REQUEST" -> handleCallRequest(payload);
            case "CALL_ACCEPT" -> handleCallAccept(payload);
            case "CALL_REJECT" -> handleCallReject(payload);
            case "CALL_CANCEL" -> handleCallCancel(payload);
            case "END_CALL" -> handleEndCall(payload);
            case "OFFER", "ANSWER", "CANDIDATE" -> forwardMessage(payload, type);
            default -> log.warn("Unknown message type: {}", type);
        }
    }

    // 연결 종료 처리
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserId(session);
        String partnerId = callStatusManager.getPartner(userId);

        log.info("[afterConnectionClosed] userId={}, closeCode={}, reason={}",
                userId, status.getCode(), status.getReason());

        if (partnerId != null) {
            CallManager.CallSession callSession = callStatusManager.getCallSession(userId);

            if (callSession != null && callSession.getCurrentStatus() != CallManager.Status.ENDED) {
                log.info("[afterConnectionClosed] Abnormal disconnection, waiting for reconnection...");
                callStatusManager.markDisconnected(userId);
            }
        }
        sessions.remove(userId);
        log.info("Disconnected: {}", userId);
    }
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String userId = getUserId(session);
        String partnerId = callStatusManager.getPartner(userId);
        log.error("WebSocket transport error: session={}, error={}, userId={}, partnerId={}", session.getId(), exception.getMessage(), userId, partnerId);
    }

    /* -----------------------------
     * 통화 관련 메시지 처리 로직
     * ----------------------------- */

    /** 발신자 → 수신자 통화 요청 전달 */
    private void handleCallRequest(JsonNode payload) {
        String from = payload.get("from").asText();
        String to = payload.get("to").asText();

        // 수신자가 이미 통화 중인지 확인
        if (callStatusManager.getStatus(to) == CallManager.Status.IN_CALL) {
            sendMessage(from, new Message("CALL_BUSY", to, from, "상대방이 통화 중입니다.", now()));
            return;
        }

        callStatusManager.startCall(from, to);
        sendMessage(to, new Message("CALL_INCOMING", from, to, "통화 요청이 왔습니다.", now()));
        log.info("CALL_REQUEST from {} → {}", from, to);
    }

    /** 수신자가 통화 수락 → 발신자에게 전달 */
    private void handleCallAccept(JsonNode payload) {
        String from = payload.get("from").asText();
        String to = payload.get("to").asText();

        callStatusManager.acceptCall(from);
        sendMessage(to, new Message("CALL_ACCEPTED", from, to, "통화를 수락했습니다.", now()));
        log.info("CALL_ACCEPT by {}", from);
    }

    /** 수신자가 통화 거절 → 발신자에게 전달 */
    private void handleCallReject(JsonNode payload) {
        String from = payload.get("from").asText();
        String to = payload.get("to").asText();

        callStatusManager.rejectCall(from);
        // 통화 이력 저장
        CallType callType = callStatusManager.determineCallType(from);
        videoCallService.saveVideoCall(from, to, callType);

        // session 제거
        callStatusManager.endCall(from);
        sendMessage(to, new Message("CALL_REJECTED", from, to, "통화를 거절했습니다.", now()));
        log.info("CALL_REJECT by {}", from);
    }

    /** 수신자가 통화 취소 -> 발신자에게 전달 **/
    private void handleCallCancel(JsonNode payload) {
        String from = payload.get("from").asText();
        String to = payload.get("to").asText();
        callStatusManager.cancelCall(from);
        // 통화 이력 저장
        CallType callType = callStatusManager.determineCallType(from);
        videoCallService.saveVideoCall(from, to, callType);

        // session 제거
        callStatusManager.endCall(from);
        // 상대방에게 취소 메세지 전송
        sendMessage(to, new Message("CALL_CANCELLED", from, to, "통화가 취소되었습니다.", now()));
        log.info("CALL_CANCEL by {}", from);
    }

    /** 통화 종료 시 저장 및 세션에서 제거 **/
    private void handleEndCall(JsonNode payload) {
        String from = payload.get("from").asText();
        String to = payload.get("to").asText();

        log.info("[handleEndCall] Call ended from {} to {}", from, to);

        CallManager.CallSession session = callStatusManager.getCallSession(from);

        // session이 없으면 이미 reject/cancel로 저장
        if (session == null) {
            log.info("[handleEndCall] Session not found - already saved as reject/cancel");
            return;
        }
        // session이 있으면 정상 종료
        CallType callType = callStatusManager.determineCallType(from);
        log.info("[handleEndCall] Determined call type: {}", callType);

        videoCallService.saveVideoCall(from, to, callType);
        log.info("[handleEndCall] Saved call record: {} - type={}", from, callType);
        // 상대방에게 알리기
        sendMessage(to, new Message("END_CALL", from, to, "통화가 종료되었습니다.", now()));
        // 상태 정리
        callStatusManager.endCall(from);
    }


    /**
     * SDP/ICE 관련 메시지를 상대방에게 포워딩하고, 통화 상태를 갱신
     */
    private void forwardMessage(JsonNode payload, String type) {
        String from = payload.path("from").asText(null);
        String to = payload.path("to").asText(null);

        if (from == null || to == null) {
            log.warn("Invalid payload: missing from/to field. payload={}", payload);
            return;
        }

        // 메시지 타입에 따라 상태 업데이트
        updateCallStatus(type, from, to);

        // 상대방에게 원본 메시지 전달
        sendRaw(to, makeJson(type, payload));

        log.debug("Forwarded [{}] from {} → {}", type, from, to);
    }

    /**
     * 메시지 타입별로 통화 상태를 업데이트하는 메서드
     */
    private void updateCallStatus(String type, String from, String to) {
        switch (type) {
            case "OFFER" -> {
                log.debug("OFFER: {} → {}", from, to);
            }
            case "ANSWER" -> {
                log.debug("ANSWER: {} → {}", from, to);
            }
            case "CANDIDATE" -> {
                log.trace("CANDIDATE exchange between {} ↔ {}", from, to);
            }
            default -> log.warn("Unknown message type: {}", type);
        }
    }


    /* -----------------------------
     * 메시지 전송 및 직렬화 유틸
     * ----------------------------- */
    /** DTO 메시지 직렬화 및 전송 */
    private void sendMessage(String userId, Message message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            sendRaw(userId, json);
        } catch (Exception e) {
            log.error("메시지 직렬화 실패 to {}: {}", userId, e.getMessage());
        }
    }

    /** 순수 문자열(JSON) 메시지 전송 */
    private void sendRaw(String userId, String msg) {
        WebSocketSession session = sessions.get(userId);

        log.info("[sendRaw] Attempting to send message to userId={}", userId);
        log.info("[sendRaw] Sessions in map: {}", sessions.keySet());
        log.info("[sendRaw] Target session exists: {}, isOpen: {}",
                session != null, session != null && session.isOpen());

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(msg));
                log.debug("Sent to {} : {}", userId, msg);
            } catch (Exception e) {
                log.error("메시지 전송 실패 to {}: {}", userId, e.getMessage());
            }
        } else {
            log.warn("[sendRaw] Target user not connected - userId={}, sessions available={}",
                    userId, sessions.keySet());
        }
    }

    /** 간단한 JSON 구조 생성 */
    private String makeJson(String type, JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(Map.of("type", type, "payload", payload));
        } catch (Exception e) {
            log.error("JSON 생성 실패: {}", e.getMessage());
            return "{}";
        }
    }

    /** 현재 시각 문자열 반환 */
    private String now() {
        return LocalDateTime.now().toString();
    }

    /** 쿼리 파라미터에서 userId 추출 */
    private String getUserId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("userId=")) {
            return query.substring("userId=".length());
        }
        return session.getId(); // fallback
    }
}
