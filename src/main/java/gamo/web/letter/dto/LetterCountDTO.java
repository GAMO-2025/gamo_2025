package gamo.web.letter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LetterCountDTO {
    private Long receivedCount;  // 받은 편지 개수
    private Long sentCount;      // 보낸 편지 개수
    private Long unreadCount;    // 읽지 않은 편지 개수
}
