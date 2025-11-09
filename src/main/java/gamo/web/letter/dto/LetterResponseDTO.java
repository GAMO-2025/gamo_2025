package gamo.web.letter.dto;

import gamo.web.letter.domain.InputType;
import gamo.web.letter.domain.Letter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LetterResponseDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private String letterImg;
    private LocalDateTime sentAt;
    private InputType inputType;
    private boolean cancelled;

    public static LetterResponseDTO from(Letter letter) {
        return LetterResponseDTO.builder()
                .id(letter.getId())
                .senderId(letter.getSenderId())
                .receiverId(letter.getReceiverId())
                .title(letter.getTitle())
                .content(letter.getContent())
                .letterImg(letter.getLetterImg())
                .inputType(letter.getInputType())
                .cancelled(letter.isCancelled())
                .build();
    }
}
