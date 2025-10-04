package gamo.web.photo.repository;

import gamo.web.photo.domain.Photo;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class PhotoRepository {
    @PersistenceContext
    private EntityManager em;

    public void save(Photo photo) {
        em.persist(photo);
    }
}
