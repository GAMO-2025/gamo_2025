package gamo.web.letter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LetterSseDTO {
    private String type;          // "received", "sent"
    private String action;        // "add", "cancel"
    private Long letterId;
    private String senderName;
    private String receiverName;
    private String contentPreview;
    private String title;
    private Long totalCount;
    private Long unreadCount;
    //private Boolean isRead;
    //private String readAt;
}
