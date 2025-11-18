package gamo.web.photo.repository;

import gamo.web.photo.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    //좋아요 취소:
    @Transactional
    @Modifying
    @Query("DELETE FROM Like l WHERE l.member.id = :memberId AND l.photo.photo_id = :photoId")
    void deleteByMemberAndPhoto(@Param("memberId") Long memberId, @Param("photoId") Long photoId);

    //이미 좋아요를 눌렀는지 확인
    @Query("SELECT COUNT(l) > 0 FROM Like l WHERE l.member.id = :memberId AND l.photo.photo_id = :photoId")
    boolean findByMemberAndPhoto(@Param("memberId") Long memberId, @Param("photoId") Long photoId);

    // 사진의 좋아요 개수
    @Query("SELECT COUNT(l) FROM Like l WHERE l.photo.photo_id = :photoId")
    Long countByPhoto(@Param("photoId") Long photoId);
}
