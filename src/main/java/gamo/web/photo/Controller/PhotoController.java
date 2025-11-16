package gamo.web.photo.Controller;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(value = "/photo/new/{albumId}")
    public String uploadPhoto(@PathVariable("albumId") Long albumId, @Valid PhotoForm form, BindingResult result) throws IOException {

        //사진 gcp에 업로드
        String newUrl = gcpStorageService.upload(form.getImage());
        String fileName = newUrl.substring(newUrl.lastIndexOf("/") + 1);

        Photo photo = new Photo();
        photo.setUrl(fileName);
        photo.setCreated_at(LocalDateTime.now());

        //임시
        Album tmpAlbum = new Album();
        tmpAlbum.setAlbum_id(albumId);
        Member tmpMember = new Member();
        tmpMember.setId(1L);
        photo.setAlbum(tmpAlbum);
        photo.setMember(tmpMember);

        photoService.uploadPhoto(photo);
        return "redirect:/album/" + albumId;
        //사진 url 받기
//        System.out.println(newUrl);
//        https://storage.googleapis.com/photo/gamo_bucket/33bd1600-8cee-4071-847c-5dd28de9409d
    }

    //사진 조회
    @GetMapping(value = "/photo/{photoId}")
    public String getPhoto(@PathVariable("photoId") Long photoId, Model model) {
        Photo photoData = photoService.getPhotoById(photoId);
        Long likeCount = likeService.getLikeCount(photoId);
        boolean isLikedByUser = likeService.isLikedByMemberId(1L, photoId);

        model.addAttribute("photoData", photoData);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("isLikedByUser", isLikedByUser);
        return "photo";
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


    @GetMapping(value = "/photo")
    public String photoForm(Model model) {
        model.addAttribute("photoForm", new PhotoForm());
        return "photoForm";
    }

    @GetMapping(value = "/frontend") //테스트용
    public String frontend() {
        return "checkFrontend";
    }
}
