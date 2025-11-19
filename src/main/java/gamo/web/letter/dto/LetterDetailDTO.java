package gamo.web.letter.dto;

import gamo.web.letter.domain.InputType;
import lombok.*;

@Getter
@Builder
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

    public static LetterDetailDTO of(Long id, Long senderId, Long receiverId,
                                     String senderName, String receiverName,
                                     String title, String content, String letterImg,
                                     String signedImageUrl, InputType inputType,
                                     Boolean isRead, Boolean isCancelled,
                                     String createdAt, String readAt,
                                     Boolean isSender, Boolean canCancel) {
        return LetterDetailDTO.builder()
                .id(id)
                .senderId(senderId)
                .receiverId(receiverId)
                .senderName(senderName)
                .receiverName(receiverName)
                .title(title)
                .content(content)
                .letterImg(letterImg)
                .signedImageUrl(signedImageUrl)
                .inputType(inputType)
                .isRead(isRead)
                .isCancelled(isCancelled)
                .createdAt(createdAt)
                .readAt(readAt)
                .isSender(isSender)
                .canCancel(canCancel)
                .build();
    }
}
