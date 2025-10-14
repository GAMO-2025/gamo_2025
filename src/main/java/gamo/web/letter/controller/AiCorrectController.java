package gamo.web.letter.controller;

import gamo.web.letter.dto.AiCorrectRequestDTO;
import gamo.web.letter.dto.AiCorrectResponseDTO;
import gamo.web.letter.service.AiCorrectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AiCorrectController {

    private final AiCorrectService aiCorrectService;

    private static final Logger log = LoggerFactory.getLogger(AiCorrectController.class);


    @PostMapping("/ai-correct")
    public ResponseEntity<AiCorrectResponseDTO> correctLetter(@RequestBody AiCorrectRequestDTO request) {
        log.info("[AI 교정 요청 수신] 내용: {}", request.getText());
        AiCorrectResponseDTO response = aiCorrectService.correctText(request.getText());
        log.info("[AI 교정 완료] 응답 내용: {}", response.getCorrectedText());
        return ResponseEntity.ok(response);
    }
}
