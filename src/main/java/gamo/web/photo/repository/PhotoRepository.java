package gamo.web.photo.repository;

import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class PhotoRepository {
    @PersistenceContext
    private EntityManager em;

    public void save(Photo photo) {
        em.persist(photo);
    }

    //앨범 썸네일
    public Photo findLastestByAlbumId(Long albumId) {
        List<Photo> result = em.createQuery(
                        "SELECT p FROM Photo p WHERE p.album.id = :albumId ORDER BY p.created_at DESC",
                        Photo.class)
                .setParameter("albumId", albumId)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public Photo findById(Long photoId) {
        return em.createQuery("SELECT p FROM Photo p WHERE p.photo_id = :photoId", Photo.class)
                .setParameter("photoId", photoId)
                .getSingleResult();
    }

    public List<Photo> findByAlbumIdOrderByCreatedAtDesc(Long albumId) {
        return em.createQuery(
                        "SELECT p FROM Photo p WHERE p.album.album_id = :albumId ORDER BY p.created_at DESC",
                        Photo.class)
                .setParameter("albumId", albumId)
                .getResultList();
    }

    //사진 삭제
    @Transactional
    public void deleteById(Long photoId) {
        Photo photo = findById(photoId);
        if (photo != null) {
            em.remove(photo);
        } else {
            throw new IllegalArgumentException("Photo not found");
        }
    }
}
