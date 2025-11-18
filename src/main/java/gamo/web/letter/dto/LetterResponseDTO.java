package gamo.web.letter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LetterResponseDTO {
    private Long letterId;
    private String receiverName;
}
