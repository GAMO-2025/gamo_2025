package gamo.web.letter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiCorrectResponseDTO {
    private int status;

    @JsonProperty("corrected_text")
    private String correctedText; // 교정된 텍스트

    private String detail;
}
