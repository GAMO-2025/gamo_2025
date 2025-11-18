package gamo.web.photo.Controller;

import gamo.web.photo.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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
}
