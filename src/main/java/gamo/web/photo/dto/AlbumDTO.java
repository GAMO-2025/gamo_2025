package gamo.web.photo.dto;

public class AlbumDTO {
    private Long albumId;       // 앨범 ID
    private String title;       // 앨범 제목
    private String thumbnailUrl; // 최근 업로드 사진 URL (썸네일)

    // 기본 생성자
//    public AlbumDto() {}

    // 모든 필드를 받는 생성자
    public AlbumDTO(Long albumId, String title, String thumbnailUrl) {
        this.albumId = albumId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
    }

    // Getter & Setter
    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
