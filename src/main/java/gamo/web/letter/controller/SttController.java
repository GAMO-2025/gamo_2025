package gamo.web.letter.controller;

import gamo.web.letter.service.SttService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/letter/api")
@Slf4j
public class SttController {

    private final SttService sttService;

    public SttController(SttService sttService) {
        this.sttService = sttService;
    }

    @PostMapping("/stt")
    public Map<String, String> stt(@RequestParam("voiceFile") MultipartFile voiceFile) {
        try {
            // 1. 요청 들어왔는지 확인
            log.info("STT 요청 들어옴");

            // 2. 파일 정보 출력
            if (voiceFile == null || voiceFile.isEmpty()) {
                log.warn("업로드된 파일이 없습니다!");
            } else {
                log.info("파일 이름: {}", voiceFile.getOriginalFilename());
                log.info("파일 크기: {} bytes", voiceFile.getSize());
                log.info("파일 contentType: {}", voiceFile.getContentType());
            }

            // 3. STT 변환
            String text = sttService.transcribe(voiceFile);

            // 4. 변환 결과 출력
            log.info("STT 변환 결과: {}", text);

            return Map.of("text", text);
        } catch (Exception e) {
            log.error("STT 변환 중 오류 발생", e);
            return Map.of("text", "", "error", e.getMessage());
        }
    }
}
