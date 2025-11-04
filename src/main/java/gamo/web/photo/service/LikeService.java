package gamo.web.photo.service;

import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import gamo.web.photo.domain.Like;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.repository.LikeRepository;
import gamo.web.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;
    private final PhotoRepository photoRepository;

    public boolean clickLike(Long memberId, Long photoId) {
        boolean liked = likeRepository.findByMemberAndPhoto(memberId, photoId);

        if(liked) { //이미 좋아요를 누른 경우
            likeRepository.deleteByMemberAndPhoto(memberId, photoId);
            return false;
        } else { //아직 좋아요를 누르지 않은 경우
            //member부분 수정하기
            Member member = new Member();
            member.setId(1L);
            Photo photo = photoRepository.findById(photoId);

            Like newLike = new Like();
            newLike.setMember(member);
            newLike.setPhoto(photo);
            likeRepository.save(newLike);
            return true;
        }
    }

    // 좋아요 개수 조회
    public Long getLikeCount(Long photoId) {
        return likeRepository.countByPhoto(photoId);
    }

    //특정 유저가 좋아요를 눌렀는지
    public boolean isLikedByMemberId(Long memberId, Long photoId) {
        return likeRepository.findByMemberAndPhoto(memberId, photoId);
    }
}
