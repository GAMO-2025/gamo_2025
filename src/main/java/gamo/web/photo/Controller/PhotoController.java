package gamo.web.photo.Controller;

import gamo.web.member.domain.Family;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.FamilyRepository;
import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.repository.AlbumRepository;
import gamo.web.photo.service.PhotoService;
import gamo.web.photo.service.GcpStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PhotoController {
    private final GcpStorageService gcpStorageService;
    private final PhotoService photoService;
    private final FamilyRepository familyRepository;
    private final AlbumRepository albumRepository; //임시
    // 생성자 주입
//    public PhotoController(GcpStorageService gcpStorageService, AlbumService albumService) {
//        this.gcpStorageService = gcpStorageService;
//        this.albumService = albumService;
//    }

    @GetMapping(value = "/album")
    public String getAlbums(Model model) {
        Long testFamilyId = 1L; //임시
        List<Album> albums = photoService.getAlbumsByFamilyId(testFamilyId);

        model.addAttribute("albums", albums);
        return "album";
    }

    @PostMapping(value = "/album/new")
    public String createAlbum(@RequestParam("title") String title) {
        Album album = new Album();
        album.setTitle(title);

        Family family = familyRepository.findById(1L) //임시로 넣어둠
                .orElseThrow(() -> new IllegalArgumentException("Family not found"));
        album.setFamily(family);

        photoService.createAlbum(album);
        return "redirect:/album";
    }

    @GetMapping(value = "/album/{albumId}")
    public String getPhotosInAlbum(@PathVariable("albumId") Long albumId, Model model) {
        List<Photo> photoList = photoService.getPhotoListByAlbumId(albumId);

        model.addAttribute("photoList", photoList);
        return "photoList";
    }

    @GetMapping(value = "/photo/{photoId}")
    public String getPhoto(@PathVariable("photoId") Long photoId, Model model) {
        Photo photoData = photoService.getPhoto(photoId);

        model.addAttribute("photoData", photoData);
        return "photo";
    }


//    --------------------

    @GetMapping(value = "/photo")
    public String photoForm(Model model) {
        model.addAttribute("photoForm", new PhotoForm());
        return "photoForm";
    }

    @PostMapping(value = "/photo/new")
    public String uploadPhoto(@Valid PhotoForm form, BindingResult result) throws IOException {

        //사진 gcp에 업로드
        String newUrl = gcpStorageService.upload(form.getImage());
        String fileName = newUrl.substring(newUrl.lastIndexOf("/") + 1);

        Photo photo = new Photo();
        photo.setUrl(fileName);
        photo.setCreated_at(LocalDateTime.now());

        //임시
       Album tmpAlbum = new Album();
       tmpAlbum.setAlbum_id(1L);
       Member tmpMember = new Member();
       tmpMember.setId(1L);
       photo.setAlbum(tmpAlbum);
       photo.setMember(tmpMember);

        photoService.uploadPhoto(photo);
        return "redirect:/album/" + tmpAlbum.getAlbum_id();
        //사진 url 받기
//        System.out.println(newUrl);
//        https://storage.googleapis.com/photo/gamo_bucket/33bd1600-8cee-4071-847c-5dd28de9409d
    }
}
