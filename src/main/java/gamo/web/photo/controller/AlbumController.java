package gamo.web.photo.controller;

import gamo.web.common.exception.CustomException;
import gamo.web.member.service.MemberService;
import gamo.web.photo.dto.AlbumDTO;
import gamo.web.photo.dto.PhotoInAlbumDTO;
import gamo.web.photo.service.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import gamo.web.auth.UserPrincipal;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AlbumController {
    private final PhotoService photoService;
    private final MemberService memberService;

    //앨범 리스트
    @GetMapping(value = "/album")
    public String getAlbums(@AuthenticationPrincipal UserPrincipal user, Model model) {
        if(user == null) return "login"; //로그인 안한 경우

        Long familyId = memberService.getFamilyId(user.getMember().getId());
        if(familyId == null) return "redirect:/member/invite"; //가족이 없는 경우

        List<AlbumDTO> albumDTOS = photoService.getAlbumsOfFamily(familyId);
        model.addAttribute("albums", albumDTOS);
        return "pages/photo/album";
    }

    //앨범 생성
    @PostMapping(value = "/album/new")
    public String createAlbum(@AuthenticationPrincipal UserPrincipal user,
                              @RequestParam("newTitle") String title) {
        Long memberId = memberService.getMemberId(user);
        photoService.createAlbum(title, memberId);
        return "redirect:/album";
    }

//    //앨범 삭제
    @PostMapping(value = "/album/delete")
    public String deleteAlbum(@AuthenticationPrincipal UserPrincipal user,
                                      @RequestParam("albumId") Long albumId,
                                         RedirectAttributes redirectAttributes) {
        try {
            Long memberId = user.getMember().getId();
            photoService.deleteAlbum(memberId, albumId);
            redirectAttributes.addFlashAttribute("successMessage", "앨범이 삭제되었습니다.");
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/album";
    }

    //앨범에 있는 사진들
    @GetMapping(value = "/album/{albumId}")
    public String getPhotosInAlbum(@AuthenticationPrincipal UserPrincipal user,
                                   @PathVariable("albumId") Long albumId,
                                   Model model) {

        Long memberId = user.getMember().getId();

        Page<PhotoInAlbumDTO> photoList = photoService.getPhotoListByAlbumId(memberId, albumId, 0, 20);

        model.addAttribute("photoList", photoList);
        model.addAttribute("albumId", albumId);
        return "pages/photo/photoList";
    }

    //앨범의 사진 : 무한 스크롤
    @GetMapping(value = "/album/{albumId}/paged")
    @ResponseBody
    public Page<PhotoInAlbumDTO> getPhotosInAlbumPaged(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable("albumId") Long albumId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {

        Long memberId = user.getMember().getId();
        return photoService.getPhotoListByAlbumId(memberId, albumId, page, size);
    }
}
