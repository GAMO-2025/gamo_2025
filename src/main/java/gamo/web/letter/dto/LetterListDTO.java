package gamo.web.letter.dto;

import gamo.web.letter.domain.Letter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LetterListDTO {
    private Long id;
    private String otherPersonName;  // 상대방 이름
    private String title;
    private String contentPreview;   // 내용 미리보기
    private String date;             // 날짜 (yyyy.MM.dd)
    private Boolean isRead;
    private Boolean isCancelled;
    private Boolean canCancel;       // 전송 취소 가능 여부 (5분 이내)

    // Entity를 DTO로 변환 (받은 편지용)
    public static LetterListDTO fromReceivedLetter(Letter letter, String senderName) {
        LetterListDTO dto = new LetterListDTO();
        dto.setId(letter.getId());
        dto.setOtherPersonName(senderName);
        dto.setTitle(letter.getTitle());
        dto.setContentPreview(getPreview(letter.getContent()));
        dto.setDate(formatDate(letter.getCreatedAt()));
        dto.setIsRead(letter.isRead());
        dto.setIsCancelled(letter.isCancelled());
        dto.setCanCancel(false); // 받은 편지는 취소 불가
        return dto;
    }

    // Entity를 DTO로 변환 (보낸 편지용)
    public static LetterListDTO fromSentLetter(Letter letter, String receiverName) {
        LetterListDTO dto = new LetterListDTO();
        dto.setId(letter.getId());
        dto.setOtherPersonName(receiverName + "에게");
        dto.setTitle(letter.getTitle());
        dto.setContentPreview(getPreview(letter.getContent()));
        dto.setDate(formatDate(letter.getCreatedAt()));
        dto.setIsRead(letter.isRead());
        dto.setIsCancelled(letter.isCancelled());
        dto.setCanCancel(canCancelLetter(letter.getCreatedAt(), letter.isCancelled()));
        return dto;
    }

    // 내용 미리보기 (50자 제한)
    private static String getPreview(String content) {
        if (content == null) return "";
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }

    // 날짜 포맷팅
    private static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }

    // 전송 취소 가능 여부 체크 (5분 이내 && 취소되지 않음)
    private static Boolean canCancelLetter(LocalDateTime createdAt, boolean isCancelled) {
        if (isCancelled || createdAt == null) return false;
        long minutesPassed = ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
        return minutesPassed < 5;
    }
}
