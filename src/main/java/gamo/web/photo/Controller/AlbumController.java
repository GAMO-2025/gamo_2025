package gamo.web.photo.Controller;

import gamo.web.family.service.FamilyService;
import gamo.web.member.service.MemberService;
import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.dto.AlbumDTO;
import gamo.web.photo.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import gamo.web.auth.UserPrincipal;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AlbumController {
    private final PhotoService photoService;
    private final MemberService memberService;
    private final FamilyService familyService;

    //앨범 리스트
    @GetMapping(value = "/album") //앨범 리스트
    public String getAlbums(@AuthenticationPrincipal UserPrincipal user, Model model) {
        Long memberId = user.getMember().getId();
        Long familyId = memberService.getFamilyId(memberId);
        List<Album> albums = photoService.getAlbumsByFamilyId(familyId);

        List<AlbumDTO> albumDtos = albums.stream().map(album -> {
            Photo latestPhoto = photoService.getLatestPhotoByAlbumId(album.getAlbum_id());
            String thumbnailUrl = (latestPhoto != null) ? latestPhoto.getUrl() : null;
            return new AlbumDTO(album.getAlbum_id(), album.getTitle(), thumbnailUrl);
        }).collect(Collectors.toList());

        model.addAttribute("albums", albumDtos);
        return "pages/photo/album";
    }

    //앨범 만들기
    @PostMapping(value = "/album/new")
    public String createAlbum(@RequestParam("newTitle") String title, @AuthenticationPrincipal UserPrincipal user) {
//        Long memberId = 2L; //임시
        Long memberId = memberService.getMemberId(user);

        photoService.createAlbum(title, memberId);
        return "redirect:/album";
    }

    //앨범 삭제
    @PostMapping(value = "/album/delete")
    public String deleteAlbum(@RequestParam("albumId") Long albumId) {
        photoService.deleteAlbum(albumId);

        return "redirect:/album";
    }

    //앨범에 있는 사진들
    @GetMapping(value = "/album/{albumId}")
    public String getPhotosInAlbum(@AuthenticationPrincipal UserPrincipal user,
                                   @PathVariable("albumId") Long albumId,
                                   Model model) {
        //권한 체크
        Long familyId_user = user.getMember().getId();
        Long familyId_album = photoService.getFamilyIdByAlbumId(albumId);
        if(familyId_user != familyId_album) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 앨범에 접근할 권한이 없습니다.");
        }

        Page<Photo> photoList = photoService.getPhotoListByAlbumId(albumId, 0, 20);

        model.addAttribute("photoList", photoList);
        return "pages/photo/photoList";
    }

    //앨범의 사진 : 무한 스크롤
    @GetMapping(value = "/album/{albumId}/paged")
    @ResponseBody
    public Page<Photo> getPhotosInAlbumPaged(
            @PathVariable("albumId") Long albumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return photoService.getPhotoListByAlbumId(albumId, page, size);
    }
}
