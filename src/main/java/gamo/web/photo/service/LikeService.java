package gamo.web.photo.service;

import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import gamo.web.photo.domain.Like;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.repository.LikeRepository;
import gamo.web.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    private final LikeRepository likeRepository;
    private final PhotoRepository photoRepository;
    private final MemberRepository memberRepository;

    public boolean clickLike(Long memberId, Long photoId) {
//        boolean liked = likeRepository.findByMemberAndPhoto(memberId, photoId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new CustomException((ErrorCode.PHOTO_NOT_FOUND)));
        Boolean liked = likeRepository.existsByMemberAndPhoto(member, photo);

        if(liked) { //이미 좋아요를 누른 경우
            likeRepository.deleteByMemberAndPhoto(member, photo);
            return false;

        } else { //아직 좋아요를 누르지 않은 경우
            Like newLike = new Like();
            newLike.setMember(member);
            newLike.setPhoto(photo);
            likeRepository.save(newLike);
            return true;
        }
    }

    // 좋아요 개수 조회
    public Long getLikeCount(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new CustomException((ErrorCode.PHOTO_NOT_FOUND)));
        return likeRepository.countByPhoto(photo);
    }

    //특정 유저가 좋아요를 눌렀는지
    public boolean isLikedByMemberId(Long memberId, Long photoId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new CustomException((ErrorCode.PHOTO_NOT_FOUND)));

        if (likeRepository.existsByMemberAndPhoto(member, photo) == null)
            return true;
        else return false;
    }
}
