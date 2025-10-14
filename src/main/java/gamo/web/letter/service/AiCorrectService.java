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

    private final String PYTHON_SERVER_URL = "http://34.64.181.41:8000/api/letter";

    public AiCorrectResponseDTO correctText(String text) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(PYTHON_SERVER_URL));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("[Python 서버로 요청 전송] URL={}, 요청본문={}", PYTHON_SERVER_URL, requestBody);
            ResponseEntity<Map> response = restTemplate.exchange(
                    PYTHON_SERVER_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            log.info("[Python 서버 응답 수신] 상태={}, 응답본문={}", response.getStatusCode(), response.getBody());

            Map<String, Object> body = response.getBody();
            AiCorrectResponseDTO dto = new AiCorrectResponseDTO();

            if (body != null) {
                dto.setStatus((Integer) body.getOrDefault("status", null));
                dto.setCorrectedText((String) body.getOrDefault("corrected_text", null));
                dto.setDetail((String) body.getOrDefault("detail", null));
            }

            return dto;

        } catch (Exception e) {
            log.error("[AI 교정 요청 중 오류 발생]: {}", e.getMessage(), e);
            AiCorrectResponseDTO errorResponse = new AiCorrectResponseDTO();
            errorResponse.setStatus(500);
            errorResponse.setDetail("AI 교정 중 오류 발생: " + e.getMessage());
            return errorResponse;
        }
    }
}