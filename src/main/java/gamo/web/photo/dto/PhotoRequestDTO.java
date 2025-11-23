package gamo.web.photo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PhotoRequestDTO {
    private Long photoId;
    private String url;
    private LocalDateTime created_at;

    private Long likeCount;
    private boolean isLikedByUser;
}
