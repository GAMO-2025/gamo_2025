package gamo.web.photo.repository;

import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PhotoRepository {
    @PersistenceContext
    private EntityManager em;

    public void save(Photo photo) {
        em.persist(photo);
    }

    public List<Photo> findByAlbumId(Long albumId) {
        return em.createQuery("SELECT p FROM Photo p WHERE p.album.id = :albumId", Photo.class)
                .setParameter("albumId", albumId)
                .getResultList();
    }

    public Photo findById(Long photoId) {
        return em.createQuery("SELECT p FROM Photo p WHERE p.photo_id = :photoId", Photo.class)
                .setParameter("photoId", photoId)
                .getSingleResult();
    }
}
