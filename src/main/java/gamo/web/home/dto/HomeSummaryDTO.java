package gamo.web.home.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeSummaryDTO {

    //앨범 썸네일
    private final String AlbumThumbnail;

    //받은 편지 개수
    private final long unreadLetterCount;
}
