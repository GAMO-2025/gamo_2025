package gamo.web.photo.repository;

import gamo.web.photo.domain.Like;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class LikeRepository {
    @PersistenceContext
    private EntityManager em;

    //좋아요
    public void save(Like like) {
        em.persist(like);
    }

    //좋아요 취소: 영속성
    public void deleteByMemberAndPhoto(Long memberId, Long photoId) {
        em.createQuery("DELETE FROM Like l WHERE l.member.id = :memberId AND l.photo.photo_id = :photoId")
                .setParameter("memberId", memberId)
                .setParameter("photoId", photoId)
                .executeUpdate();
    }

    //이미 좋아요를 눌렀는지 확인
    public boolean findByMemberAndPhoto(Long memberId, Long photoId) {
        String jpql = "SELECT COUNT(l) FROM Like l WHERE l.member.id = :memberId AND l.photo.photo_id = :photoId";
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("memberId", memberId)
                .setParameter("photoId", photoId)
                .getSingleResult();
        return count > 0;
    }

    // 사진의 좋아요 개수
    public Long countByPhoto(Long photoId) {
        String jpql = "SELECT COUNT(l) FROM Like l WHERE l.photo.photo_id = :photoId";
        return em.createQuery(jpql, Long.class)
                .setParameter("photoId", photoId)
                .getSingleResult();
    }
}
