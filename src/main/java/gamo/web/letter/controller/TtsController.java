package gamo.web.letter.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.letter.dto.LetterDetailDTO;
import gamo.web.letter.service.LetterService;
import gamo.web.letter.service.TtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/letter/api")
public class TtsController {

    private final TtsService ttsService;
    private final LetterService letterService;

    @GetMapping("/tts/{letterId}")
    public ResponseEntity<?> getLetterAudio(
            @PathVariable Long letterId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getMember().getId();

        // 편지 내용 가져오기 + 권한 체크
        LetterDetailDTO dto = letterService.getLetterDetail(letterId, userId);

        try {
            String base64Audio = ttsService.synthesizeSpeech(dto.getContent());
            return ResponseEntity.ok(Map.of("audioBase64", base64Audio));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("TTS_ERROR");
        }
    }
}

