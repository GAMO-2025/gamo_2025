package gamo.web.photo.repository;

import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // 앨범의 가장 최근 사진 1장
    Optional<Photo> findTop1ByAlbumOrderByCreatedAtDesc(Album album);

    // photo_id로 Photo 조회
    Optional<Photo> findById(Long id);

    // 앨범의 사진 목록 + 페이징
    @Query("SELECT p FROM Photo p WHERE p.album.album_id = :albumId")
    Page<Photo> findPhotosByAlbumId(@Param("albumId") Long albumId, Pageable pageable);

    @Query("SELECT p.album.family.id FROM Photo p WHERE p.photo_id = :photoId")
    Long findFamilyIdByPhotoId(@Param("photoId") Long photoId);

    @Query("SELECT p.member.id FROM Photo p WHERE p.photo_id = :photoId")
    Long findMemberById(@Param("photoId") Long photoId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Photo p WHERE p.album.id = :albumId")
    boolean existsByAlbumId(@Param("albumId") Long albumId);

    Photo findByUrl(String url);

    @Query("SELECT p.album.album_id FROM Photo p WHERE p.photo_id = :photoId")
    Long findAlbumIdByPhotoId(@Param("photoId") Long photoId);
}
