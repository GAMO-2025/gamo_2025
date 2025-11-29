package gamo.web.home.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeSummaryDTO {
    //가장 최근에 전화한 사람
    private final String targetNickname;
    private final String targetProfileImage;

    //앨범 썸네일
    private final String AlbumThumbnail;

    //통화 내용 추천
    private final String ajenda;

    //받은 편지 개수
    private final long unreadLetterCount;
}
