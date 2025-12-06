package gamo.web.videocall.service;

import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.videocall.dto.RecommendDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopicService {

    private static final String AJENDA_API_URL = "http://34.158.203.193:8000/api/ajenda";
    private static final String TOPIC_API_URL = "http://34.158.203.193:8000/api/keyword";

    private static final int MAX_RETRY_COUNT = 3;
    private static final long INITIAL_DELAY_MS = 2000;


    /* ===========================
           🔹 Public API Methods
       =========================== */

    public RecommendDTO.RecommendResponse getRecommendedTopic(RecommendDTO.RecommendRequest request) {
        log.info("TopicService: 추천 주제 요청 시작 - videoCallIds={}", request.getVideoCallIds());
        return callApiWithRetry(AJENDA_API_URL, request, 1);
    }

    public void sendTopicRequest(RecommendDTO.TopicRequest request) {
        log.info("TopicService: 키워드 분석 요청 시작 - videoCallId={}", request.getCallId());
        callApiWithRetry(TOPIC_API_URL, request, 1);
    }

    /* ===========================
           🔹 공통 API 호출 로직
       =========================== */

    private RecommendDTO.RecommendResponse callApiWithRetry(
            String url,
            Object requestBody,
            int attempt
    ) {
        log.info("TopicService: API 호출 시도 {}/{}", attempt, MAX_RETRY_COUNT);

        try {
            Map<String, Object> responseBody = sendPost(url, requestBody);
            return parseRecommendResponse(responseBody);

        } catch (Exception e) {
            log.error("API 요청 실패 (attempt {}): {}", attempt, e.getMessage());

            if (attempt >= MAX_RETRY_COUNT) {
                throw new CustomException(ErrorCode.TOPIC_RECOMMENDATION_FAILED);
            }

            sleepForRetry(attempt);
            return callApiWithRetry(url, requestBody, attempt + 1);
        }
    }

    // post 호출 공통 함수
    private Map<String, Object> sendPost(String url, Object requestBody) {
        log.info("TopicService: 요청 전송 - URL={}, body={}", url, requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Map.class
        );

        log.info("TopicService: 응답 수신 - statusCode={}", response.getStatusCode());

        return response.getBody();
    }

    // 응답 파싱 공통 함수
    private RecommendDTO.RecommendResponse parseRecommendResponse(Map<String, Object> body) {

        if (body == null) {
            throw new CustomException(ErrorCode.TOPIC_RECOMMENDATION_FAILED);
        }

        Integer status = extractStatus(body);
        String topic = (String) body.get("recommended_topic");

        log.info("TopicService: 응답 파싱 완료 - status={}, topic={}", status, topic);

        return RecommendDTO.RecommendResponse.builder()
                .status(status)
                .recommendedTopic(topic)
                .build();
    }

    private Integer extractStatus(Map<String, Object> body) {
        Object rawStatus = body.get("status");

        if (rawStatus instanceof Integer) {
            return (Integer) rawStatus;
        }
        if (rawStatus instanceof Number) {
            return ((Number) rawStatus).intValue();
        }
        if (rawStatus instanceof String) {
            return Integer.parseInt((String) rawStatus);
        }

        return null;
    }

    /* ===========================
           🔹 Retry 딜레이 적용
       =========================== */

    private void sleepForRetry(int attempt) {
        long delay = INITIAL_DELAY_MS * (long) Math.pow(2, attempt - 1);

        try {
            log.warn("TopicService: {}ms 후 재시도 예정...", delay);
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}