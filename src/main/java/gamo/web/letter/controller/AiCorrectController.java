package gamo.web.letter.controller;

import gamo.web.letter.dto.AiCorrectRequestDTO;
import gamo.web.letter.dto.AiCorrectResponseDTO;
import gamo.web.letter.service.AiCorrectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/letter/api")
public class AiCorrectController {

    private final AiCorrectService aiCorrectService;

    @PostMapping("/ai-correct")
    public ResponseEntity<AiCorrectResponseDTO> correctLetter(@RequestBody AiCorrectRequestDTO request) {
        log.info("[AI 교정 요청 수신] 내용: {}", request.getText());
        AiCorrectResponseDTO response = aiCorrectService.correctText(request.getText());
        log.info("[AI 교정 완료] 응답 내용: {}", response.getCorrectedText());
        return ResponseEntity.ok(response);
    }
}
