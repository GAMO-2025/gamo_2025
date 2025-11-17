package gamo.web.letter.dto;

import gamo.web.letter.domain.Letter;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LetterListDTO {
    private Long id;
    private String otherPersonName;  // 상대방 이름
    private String title;
    private String contentPreview;   // 내용 미리보기
    private String date;             // 날짜 (yyyy.MM.dd)
    private Boolean isRead;
    private Boolean isCancelled;
    private Boolean canCancel;       // 전송 취소 가능 여부 (5분 이내)

    // Entity → DTO 변환 (받은 편지용)
    public static LetterListDTO fromReceivedLetter(Letter letter, String senderName) {
        return LetterListDTO.builder()
                .id(letter.getId())
                .otherPersonName(senderName)
                .title(letter.getTitle())
                .contentPreview(getPreview(letter.getContent()))
                .date(formatDate(letter.getCreatedAt()))
                .isRead(letter.isRead())
                .isCancelled(letter.isCancelled())
                .canCancel(false) // 받은 편지는 취소 불가
                .build();
    }

    // Entity → DTO 변환 (보낸 편지용)
    public static LetterListDTO fromSentLetter(Letter letter, String receiverName) {
        return LetterListDTO.builder()
                .id(letter.getId())
                .otherPersonName(receiverName + "에게")
                .title(letter.getTitle())
                .contentPreview(getPreview(letter.getContent()))
                .date(formatDate(letter.getCreatedAt()))
                .isRead(letter.isRead())
                .isCancelled(letter.isCancelled())
                .canCancel(canCancelLetter(letter.getCreatedAt(), letter.isCancelled()))
                .build();
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