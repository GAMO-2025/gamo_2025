package gamo.web.photo.service;

import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.repository.AlbumRepository;
import gamo.web.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
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

    //앨범 썸네일
    public Photo getLatestPhotoByAlbumId(Long albumId) {
        return photoRepository.findLatestByAlbumId(albumId);
    }

    @Transactional
    public void deleteAlbum(Long albumId) {
        albumRepository.deleteById(albumId);
//        System.out.println("[Service] 앨범 삭제 완료 - ID: " + albumId);
    }

    //사진 업로드
    @Transactional
    public void uploadPhoto(Photo photo) {
        photoRepository.save(photo);
    }

    //사진 상세
    public Photo getPhotoById(Long photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("사진을 찾을 수 없습니다."));
    }

    //사진 삭제
    @Transactional
    public Long deletePhoto(Long id) {
        Photo photo = photoRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("사진을 찾을 수 없습니다."));
        Long albumId = photo.getAlbum().getAlbum_id();
        photoRepository.deleteById(id);
        return albumId;
    }
}
