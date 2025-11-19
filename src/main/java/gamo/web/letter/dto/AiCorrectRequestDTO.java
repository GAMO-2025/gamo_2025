package gamo.web.letter.dto;

import lombok.*;

@Getter
public class AiCorrectRequestDTO {
    private String text; // 교정할 원본 텍스트
}