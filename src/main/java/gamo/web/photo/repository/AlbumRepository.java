package gamo.web.photo.repository;

import gamo.web.photo.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    @Query("SELECT a FROM Album a WHERE a.family.id = :familyId")
    List<Album> findByFamilyId(@Param("familyId") Long familyId);

}