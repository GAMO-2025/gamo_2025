package gamo.web.photo.repository;

import gamo.web.photo.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    @Query("SELECT a FROM Album a WHERE a.family.id = :familyId")
    List<Album> findByFamilyId(@Param("familyId") Long familyId);

    // albumId로 familyId 조회
    @Query("SELECT a.family.id FROM Album a WHERE a.album_id = :albumId")
    Long findFamilyIdById(@Param("albumId") Long albumId);

    @Query("SELECT a FROM Album a WHERE a.family.id = :familyId ORDER BY a.album_id DESC")
    Optional<Album> findLatestAlbumByFamilyId(@Param("familyId") Long familyId);
}