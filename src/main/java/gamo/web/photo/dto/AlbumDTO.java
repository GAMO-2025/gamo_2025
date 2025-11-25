package gamo.web.photo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlbumDTO {
    private Long albumId;       // 앨범 ID
    private String title;       // 앨범 제목
    private String thumbnailUrl; // 최근 업로드 사진 URL (썸네일)
}
