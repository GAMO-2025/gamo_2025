package gamo.web.photo.service;

import com.google.cloud.storage.BlobId;
import gamo.web.member.domain.Family;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.repository.AlbumRepository;
import gamo.web.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;
    private final MemberRepository memberRepository;

    //앨범 생성
    @Transactional
    public void createAlbum(String title, Long memberId) {
        Album album = new Album();
        album.setTitle(title);

        Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (member.getFamily() == null) {
            throw new IllegalStateException("회원이 가족에 속해 있지 않습니다.");
        }
        album.setFamily(member.getFamily());

        albumRepository.save(album);
    }

    //앨범 조회
    public List<Album> getAlbumsByFamilyId(Long familyId) {
        return albumRepository.findByFamilyId(familyId);
    }

    // Photo 리스트 조회 (최신순)
    public List<Photo> getPhotoListByAlbumId(Long albumId) {
        return photoRepository.findByAlbumIdOrderByCreatedAtDesc(albumId);
    }

    public Photo getLatestPhotoByAlbumId(Long albumId) {
        return photoRepository.findLastestByAlbumId(albumId);
    }

    @Transactional
    public void deleteAlbum(Long albumId) {
        albumRepository.deleteByAlbumId(albumId);
        System.out.println("[Service] 앨범 삭제 완료 - ID: " + albumId);
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
