package gamo.web.letter.controller;

import gamo.web.letter.service.SttService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stt")
public class SttController {

    private final SttService sttService;

    public SttController(SttService sttService) {
        this.sttService = sttService;
    }

    @PostMapping
    public Map<String, String> stt(@RequestParam("voiceFile") MultipartFile voiceFile) {
        try {
            // 1. 요청 들어왔는지 확인
            System.out.println("STT 요청 들어옴");

            // 2. 파일 정보 출력
            if (voiceFile == null || voiceFile.isEmpty()) {
                System.out.println("업로드된 파일이 없습니다!");
            } else {
                System.out.println("파일 이름: " + voiceFile.getOriginalFilename());
                System.out.println("파일 크기: " + voiceFile.getSize());
                System.out.println("파일 contentType: " + voiceFile.getContentType());
            }

            // 3. STT 변환
            String text = sttService.transcribe(voiceFile);

            // 4. 변환 결과 출력
            System.out.println("STT 변환 결과: " + text);

            return Map.of("text", text);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("text", "", "error", e.getMessage());
        }
    }
}

