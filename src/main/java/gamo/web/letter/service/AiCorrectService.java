package gamo.web.letter.service;

import gamo.web.letter.dto.AiCorrectResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AiCorrectService {

    private final String PYTHON_SERVER_URL = "http://34.158.203.193:8000/api/letter";

    public AiCorrectResponseDTO correctText(String text) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(PYTHON_SERVER_URL));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        long startTime = System.currentTimeMillis(); // 시작 시간

        try {
            log.info("[Python 서버로 요청 전송] URL={}, 요청본문={}", PYTHON_SERVER_URL, requestBody);

            ResponseEntity<Map> response = restTemplate.exchange(
                    PYTHON_SERVER_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            long endTime = System.currentTimeMillis(); // 끝 시간
            long duration = endTime - startTime;
            log.info("[Python 서버 응답 수신] 상태={}, 응답본문={}", response.getStatusCode(), response.getBody());
            log.info("[요청 처리 시간] {} ms", duration); // 처리 시간 로그 출력

            Map<String, Object> body = response.getBody();
            AiCorrectResponseDTO dto = new AiCorrectResponseDTO();

            if (body != null) {
                dto.setStatus((Integer) body.getOrDefault("status", null));
                dto.setCorrectedText((String) body.getOrDefault("corrected_text", null));
                dto.setDetail((String) body.getOrDefault("detail", null));
            }

            return dto;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.error("[AI 교정 요청 중 오류 발생]: {}, 소요 시간={} ms", e.getMessage(), duration, e);

            AiCorrectResponseDTO errorResponse = new AiCorrectResponseDTO();
            errorResponse.setStatus(500);
            errorResponse.setDetail("AI 교정 중 오류 발생: " + e.getMessage());
            return errorResponse;
        }
    }
}