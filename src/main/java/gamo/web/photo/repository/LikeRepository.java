package gamo.web.photo.repository;

import gamo.web.member.domain.Member;
import gamo.web.photo.domain.Like;
import gamo.web.photo.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface LikeRepository extends JpaRepository<Like, Long> {
    //좋아요 취소: 튜플 삭제
    @Modifying
    void deleteByMemberAndPhoto(Member member, Photo photo);

    //사진 조회: 사진의 좋아요 개수
    Long countByPhoto(Photo photo);

    //사진 조회: 이미 좋아요를 눌렀는지 확인
    Boolean existsByMemberAndPhoto(Member member, Photo photo);
}
