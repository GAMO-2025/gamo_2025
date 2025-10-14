package gamo.web.letter.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class LetterRequestDTO {
    private Long receiverId;
    private String title;
    private String content;
    private String inputType; // TEXT / VOICE
    private MultipartFile voiceFile; // STT용
    private MultipartFile letterImg;  // 이미지
}
