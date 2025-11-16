package gamo.web.photo.Controller;

import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.dto.AlbumDto;
import gamo.web.photo.service.GcpStorageService;
import gamo.web.photo.service.LikeService;
import gamo.web.photo.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AlbumController {
    private final GcpStorageService gcpStorageService;
    private final PhotoService photoService;
    private final LikeService likeService;

    //앨범 리스트
    @GetMapping(value = "/album") //앨범 리스트
    public String getAlbums(Model model) {
        Long testFamilyId = 1L; // 임시
        List<Album> albums = photoService.getAlbumsByFamilyId(testFamilyId);

        List<AlbumDto> albumDtos = albums.stream().map(album -> {
            Photo latestPhoto = photoService.getLatestPhotoByAlbumId(album.getAlbum_id());
            String thumbnailUrl = (latestPhoto != null) ? latestPhoto.getUrl() : null;
            return new AlbumDto(album.getAlbum_id(), album.getTitle(), thumbnailUrl);
        }).collect(Collectors.toList());

        model.addAttribute("albums", albumDtos);
        return "album";
    }

    //앨범 만들기
    @PostMapping(value = "/album/new")
    public String createAlbum(@RequestParam("newTitle") String title) {
        Long memberId = 2L; //임시

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
    public String getPhotosInAlbum(@PathVariable("albumId") Long albumId, Model model) {
        List<Photo> photoList = photoService.getPhotoListByAlbumId(albumId);

        model.addAttribute("photoList", photoList);
        return "photoList";
    }
}
