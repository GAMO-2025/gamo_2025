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
@RequiredArgsConstructor
@Slf4j
public class TopicService {

    private static final String AJENDA_API_URL = "http://34.158.203.193:8000/api/ajenda";
    private static final int MAX_RETRY_COUNT = 3;
    private static final long INITIAL_DELAY_MS = 2000;

    public RecommendDTO.RecommendResponse getRecommendedTopic(
            RecommendDTO.RecommendRequest request) {
        log.info("TopicService: 추천 주제 조회 시작 - videoCallIds={}", request.getVideoCallIds());
        return retryApiCall(request, 1);
    }

    private RecommendDTO.RecommendResponse retryApiCall(
            RecommendDTO.RecommendRequest request,
            int attemptCount) {
        log.info("TopicService: API 호출 시도 - {}/{}", attemptCount, MAX_RETRY_COUNT);

        long startTime = System.currentTimeMillis();

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RecommendDTO.RecommendRequest> entity = new HttpEntity<>(request, headers);
            log.info("TopicService: 요청 전송 - URL={}, 요청본문={}", AJENDA_API_URL, request.getVideoCallIds());

            ResponseEntity<Map> response = restTemplate.exchange(
                    AJENDA_API_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            long duration = System.currentTimeMillis() - startTime;
            log.info("TopicService: API 응답 수신 - statusCode={}, 소요시간={}ms",
                    response.getStatusCode(), duration);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null) {
                log.error("TopicService: 응답 본문이 비어있습니다");
                return handleRetryOrFail(request, attemptCount, "응답 본문이 null입니다");
            }

            RecommendDTO.RecommendResponse result = RecommendDTO.RecommendResponse.builder()
                    .status((Integer) responseBody.getOrDefault("status", null))
                    .recommendedTopic((String) responseBody.getOrDefault("recommended_topic", null))
                    .build();

            if (result.getStatus() == 200) {
                log.info("TopicService: 추천 주제 조회 성공 - topic={}", result.getRecommendedTopic());
                return result;
            }

            if (result.getStatus() == 500) {
                log.warn("TopicService: API 서버 에러 발생 - status=500");
                return handleRetryOrFail(request, attemptCount, "API 서버 에러");
            }

            log.error("TopicService: 예상치 못한 상태코드 - status={}", result.getStatus());
            throw new CustomException(ErrorCode.TOPIC_RECOMMENDATION_FAILED);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("TopicService: API 요청 중 오류 발생 - type={}, message={}, 소요시간={}ms",
                    e.getClass().getSimpleName(), e.getMessage(), duration, e);

            return handleRetryOrFail(request, attemptCount, e.getMessage());
        }
    }

    private RecommendDTO.RecommendResponse handleRetryOrFail(
            RecommendDTO.RecommendRequest request,
            int attemptCount,
            String errorMessage) {

        if (attemptCount < MAX_RETRY_COUNT) {
            long delayMs = INITIAL_DELAY_MS * (long) Math.pow(2, attemptCount - 1);
            log.warn("TopicService: {}ms 후 재시도 예정 ({}/{})", delayMs, attemptCount, MAX_RETRY_COUNT);

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("TopicService: 재시도 대기 중 인터럽트 발생");
            }

            return retryApiCall(request, attemptCount + 1);
        }

        log.error("TopicService: 최대 재시도 횟수 초과 ({}회) - {}", MAX_RETRY_COUNT, errorMessage);
        throw new CustomException(ErrorCode.TOPIC_RECOMMENDATION_FAILED);
    }
}