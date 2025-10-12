package gamo.web.photo.repository;

import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class AlbumRepository {
    @PersistenceContext
    private EntityManager em;

    public void save(Album album) { //앨범 생성
        em.persist(album);
    }

    public List<Album> findByFamilyId(Long familyId) {
        return em.createQuery("SELECT a FROM Album a WHERE a.family.id = :familyId", Album.class)
                .setParameter("familyId", familyId)
                .getResultList();
    }
}
