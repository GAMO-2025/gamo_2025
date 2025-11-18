package gamo.web.letter.dto;

import lombok.*;

@Getter
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

    public static LetterListDTO of(Long id, String otherPersonName, String title,
                                   String contentPreview, String date, Boolean isRead,
                                   Boolean isCancelled, Boolean canCancel) {
        return LetterListDTO.builder()
                .id(id)
                .otherPersonName(otherPersonName)
                .title(title)
                .contentPreview(contentPreview)
                .date(date)
                .isRead(isRead)
                .isCancelled(isCancelled)
                .canCancel(canCancel)
                .build();
    }
}