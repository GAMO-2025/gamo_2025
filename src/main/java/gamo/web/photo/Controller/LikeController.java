package gamo.web.photo.Controller;

import gamo.web.photo.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    //좋아요 버튼 클릭
    @PostMapping(value = "/photoLike/{photoId}")
    public ResponseEntity<String> clickLike(@PathVariable("photoId") Long photoId) {
        Long memberId = 1L; //임시
        boolean liked = likeService.clickLike(memberId, photoId);
        if (liked) {
            return ResponseEntity.ok("liked");
        } else {
            return ResponseEntity.ok("unliked");
        }
    }

    //사진의 좋아요 수
//    @GetMapping(value = "/photoLike/{photoId}/count")
//    public Long getLikeCount(Long photoId) {
//        return likeService.getLikeCount(photoId);
//    }
//
//    //좋아요 눌렀는지
//    @GetMapping(value = "/photoLike/{photoId}/check")
//    public ResponseEntity<Boolean> isLiked(Long memberId, Long photoId) {
//        return likeService.isLikedByMemberId(memberId, photoId);
//    }
}
