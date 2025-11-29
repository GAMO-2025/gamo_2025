package gamo.web.photo.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.member.service.MemberService;
import gamo.web.photo.dto.PhotoRequestDTO;
import gamo.web.photo.dto.PhotoUploadRequestDTO;
import gamo.web.photo.service.PhotoService;
import gamo.web.photo.service.GcpStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class PhotoController {
    private final GcpStorageService gcpStorageService;
    private final PhotoService photoService;
    private final MemberService memberService;

    //사진 업로드
    @PostMapping(value = "/photo/new/{albumId}")
    public String uploadPhoto(@AuthenticationPrincipal UserPrincipal user,
                              @PathVariable("albumId") Long albumId,
                              @Valid PhotoUploadRequestDTO photoRequestDTO) throws IOException {

        //gcp에 사진 업로드
        String fileName = gcpStorageService.upload(photoRequestDTO.getImage());

        Long memberId = user.getMember().getId();
        photoService.uploadPhoto(albumId, memberId, fileName);

        return "redirect:/album/" + albumId;
    }

    //사진 조회
    @GetMapping(value = "/photo/{photoId}")
    public String getPhoto(@AuthenticationPrincipal UserPrincipal user,
                           @PathVariable("photoId") Long photoId,
                           Model model) {

        PhotoRequestDTO photoRequestDTO = photoService.getPhotoById(user.getMember().getId(), photoId);
        model.addAttribute("photo", photoRequestDTO);
        return "pages/photo/photo";
    }

    //사진 삭제
    @DeleteMapping("photo/delete/{photoId}")
    public String deletePhoto(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable("photoId") Long photoId) throws IOException {
        Long memberId = memberService.getMemberId(user);

        gcpStorageService.delete(photoId);
        Long album_id = photoService.deletePhoto(memberId, photoId);
        return "redirect:/album/" + album_id;
    }
}
