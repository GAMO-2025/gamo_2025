package gamo.web.letter.dto;

import gamo.web.letter.domain.InputType;
import gamo.web.letter.domain.Letter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class LetterResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private String letterImg;
    private LocalDateTime sentAt;
    private InputType inputType;
    private boolean cancelled;

    public static LetterResponse from(Letter letter) {
        return new LetterResponse(
                letter.getId(),
                letter.getSenderId(),
                letter.getReceiverId(),
                letter.getTitle(),
                letter.getContent(),
                letter.getLetterImg(),
                letter.getSentAt(),
                letter.getInputType(),
                letter.isCancelled()
        );
    }
}
