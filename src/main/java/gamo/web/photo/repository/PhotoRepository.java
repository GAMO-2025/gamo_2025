package gamo.web.photo.repository;

import gamo.web.photo.domain.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // 앨범의 가장 최근 사진 1장
    @Query(value = "SELECT * FROM photo WHERE album_id = :albumId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Photo findLatestByAlbumId(@Param("albumId") Long albumId);

    // photo_id로 Photo 조회
    Optional<Photo> findById(Long id);

    // 특정 앨범의 사진 목록 — createdAt 내림차순
    //@Query("SELECT p FROM Photo p WHERE p.album.album_id = :albumId ORDER BY p.created_at DESC")
    //List<Photo> findByAlbumIdOrderByCreatedAtDesc(@Param("albumId") Long albumId);

    // 앨범의 사진 목록 + 페이징
    @Query("SELECT p FROM Photo p WHERE p.album.album_id = :albumId")
    Page<Photo> findPhotosByAlbumId(@Param("albumId") Long albumId, Pageable pageable);
}
