package gamo.web.letter.dto;

import gamo.web.letter.domain.InputType;
import gamo.web.letter.domain.Letter;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LetterDetailDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String receiverName;
    private String title;
    private String content;
    private String letterImg;
    private String signedImageUrl;
    private InputType inputType;
    private Boolean isRead;
    private Boolean isCancelled;
    private String createdAt;
    private String readAt;
    private Boolean isSender;
    private Boolean canCancel;       // 전송 취소 가능 여부 (5분 이내)

    public static LetterDetailDTO fromEntity(Letter letter, String senderName, String receiverName, Long currentUserId) {
        return LetterDetailDTO.builder()
                .id(letter.getId())
                .senderId(letter.getSenderId())
                .receiverId(letter.getReceiverId())
                .senderName(senderName)
                .receiverName(receiverName)
                .title(letter.getTitle())
                .content(letter.getContent())
                .letterImg(letter.getLetterImg())
                .inputType(letter.getInputType())
                .isRead(letter.isRead())
                .isCancelled(letter.isCancelled())
                .createdAt(formatDateTime(letter.getCreatedAt()))
                .readAt(letter.getReadAt() != null ? formatDateTime(letter.getReadAt()) : null)
                .isSender(letter.getSenderId().equals(currentUserId))
                .canCancel(canCancelLetter(letter.getCreatedAt(), letter.isCancelled()))
                .build();
    }

    public static LetterDetailDTO fromEntityWithSignedUrl(
            Letter letter, String senderName, String receiverName, Long currentUserId, String signedImageUrl) {
        LetterDetailDTO dto = fromEntity(letter, senderName, receiverName, currentUserId);
        dto.signedImageUrl = signedImageUrl;
        return dto;
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
    }

    // 전송 취소 가능 여부 체크 (5분 이내 && 취소되지 않음)
    private static Boolean canCancelLetter(LocalDateTime createdAt, boolean isCancelled) {
        if (isCancelled || createdAt == null) return false;
        long minutesPassed = ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
        return minutesPassed < 5;
    }
}
