package gamo.web.letter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
public class AiCorrectResponseDTO {
    private int status;

    @JsonProperty("corrected_text")
    private String correctedText; // 교정된 텍스트

    private String detail;
}
