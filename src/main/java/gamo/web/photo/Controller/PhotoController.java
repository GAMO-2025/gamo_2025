package gamo.web.photo.Controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.member.domain.Member;
import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.dto.AlbumDto;
import gamo.web.photo.service.LikeService;
import gamo.web.photo.service.PhotoService;
import gamo.web.photo.service.GcpStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PhotoController {
    private final GcpStorageService gcpStorageService;
    private final PhotoService photoService;
    private final LikeService likeService;

    //사진 업로드
    @PostMapping(value = "/photo/new/{albumId}")
    public String uploadPhoto(@AuthenticationPrincipal UserPrincipal user,
                              @PathVariable("albumId") Long albumId,
                              @Valid PhotoForm form, BindingResult result) throws IOException {

        //사진 gcp에 업로드
        String newUrl = gcpStorageService.upload(form.getImage());
        String fileName = newUrl.substring(newUrl.lastIndexOf("/") + 1);

        Photo photo = new Photo();
        photo.setUrl(fileName);
        photo.setCreated_at(LocalDateTime.now());

        //임시
        Album album = new Album();
        album.setAlbum_id(albumId);
        //Member tmpMember = new Member();
        //tmpMember.setId(1L);
//        Long memberId = user.getMember().getId();
        Member member = user.getMember();
        photo.setAlbum(album);
        photo.setMember(member);

        photoService.uploadPhoto(photo);
        return "redirect:/album/" + albumId;
    }

    //사진 조회
    @GetMapping(value = "/photo/{photoId}")
    public String getPhoto(@AuthenticationPrincipal UserPrincipal user,
                           @PathVariable("photoId") Long photoId,
                           Model model) {
        Long familyId_user = user.getMember().getId();
        Long familyId_photo = photoService.getFamilyIdByPhotoId(photoId);
        if(familyId_user != familyId_photo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 앨범에 접근할 권한이 없습니다.");
        }

        Photo photoData = photoService.getPhotoById(photoId);
        Long likeCount = likeService.getLikeCount(photoId);
        boolean isLikedByUser = likeService.isLikedByMemberId(1L, photoId);

        model.addAttribute("photoData", photoData);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("isLikedByUser", isLikedByUser);
        return "pages/photo/photo";
    }

    //사진 삭제
    @DeleteMapping("photo/delete/{photoId}")
    public ResponseEntity<String> deletePhoto(@PathVariable("photoId") Long photoId) {
        try {
            Photo photo = photoService.getPhotoById(photoId);
            boolean deletedFromGCS = gcpStorageService.delete(photo.getUrl());
            Long album_id = photoService.deletePhoto(photoId);

//            return ResponseEntity.ok("deleted");
            return ResponseEntity.ok("/album/" + album_id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }


//    @GetMapping(value = "/photo")
//    public String photoForm(Model model) {
//        model.addAttribute("photoForm", new PhotoForm());
//        return "pages/photo/photoForm";
//    }
}
