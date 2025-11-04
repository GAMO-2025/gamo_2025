package gamo.web.photo.service;

import com.google.cloud.storage.BlobId;
import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.repository.AlbumRepository;
import gamo.web.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;

    //앨범 생성
    @Transactional
    public void createAlbum(Album album) {
        albumRepository.save(album);
    }

    //앨범 조회
    public List<Album> getAlbumsByFamilyId(Long familyId) {
        return albumRepository.findByFamilyId(familyId);
    }

    public List<Photo> getPhotoListByAlbumId(Long albumId) {
        return photoRepository.findByAlbumId(albumId);
    }

    public Photo getLatestPhotoByAlbumId(Long albumId) {
        return photoRepository.findLastestByAlbumId(albumId);
    }

    //사진 업로드
    @Transactional
    public void uploadPhoto(Photo photo) {
        photoRepository.save(photo);
    }

    //사진 상세
    public Photo getPhotoById(Long photoId) {
        return photoRepository.findById(photoId);
    }

    public void deletePhoto(Long id) {
        photoRepository.deleteById(id);
    }
}
