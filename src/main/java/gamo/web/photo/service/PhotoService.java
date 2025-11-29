package gamo.web.photo.service;

import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.family.domain.Family;
import gamo.web.family.repository.FamilyRepository;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import gamo.web.photo.domain.Album;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.dto.AlbumDTO;
import gamo.web.photo.dto.PhotoInAlbumDTO;
import gamo.web.photo.dto.PhotoRequestDTO;
import gamo.web.photo.repository.AlbumRepository;
import gamo.web.photo.repository.LikeRepository;
import gamo.web.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {
    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;
    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final LikeRepository likeRepository;

    //family의 앨범 조회
    @Transactional(readOnly = true)
    public List<AlbumDTO> getAlbumsOfFamily(Long familyId) {
        return albumRepository.findByFamilyId(familyId)
                .stream().map(album -> new AlbumDTO(
                        album.getAlbum_id(),
                        album.getTitle(),
                        getThumbnail(album)
                )).toList();
    }

    //앨범 썸네일
    @Transactional(readOnly = true)
    public String getThumbnail(Album album) {
        Photo photo = photoRepository.findTop1ByAlbumOrderByCreatedAtDesc(album).orElse(null);
        return photo == null ? null : photo.getUrl();
    }

    @Transactional(readOnly = true)
    public String getLatestAlbumThumbnailByFamily(Long familyId) {
        List<Album> albums = albumRepository.findAlbumByFamilyId(familyId);

        if (albums.isEmpty()) {
            return null;
        }

        Album latestAlbum = albums.get(0);
        return getThumbnail(latestAlbum);
    }

        //앨범 생성
    @Transactional
    public void createAlbum(String title, Long memberId) {
        Long familyId = memberRepository.findFamilyIdById(memberId)
                .orElseThrow(() -> new CustomException((ErrorCode.FAMILY_NOT_FOUND)));
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new CustomException((ErrorCode.FAMILY_NOT_FOUND)));

        Album album = Album.builder()
                .title(title)
                .family(family)
                .build();

        albumRepository.save(album);
    }

    //앨범 삭제
    @Transactional
    public void deleteAlbum(Long memberId, Long albumId) {
        //앨범에 아무사진도 없는지 확인
        if(photoRepository.existsByAlbumId(albumId))
            throw new CustomException(ErrorCode.ALBUM_NOT_EMPTY);

        //권한 확인
        Long familyOfMember = memberRepository.findFamilyIdById(memberId)
                .orElseThrow(() -> new CustomException((ErrorCode.FAMILY_NOT_FOUND)));
        Long familyOfAlbum = albumRepository.findFamilyIdById(albumId);

        if(!familyOfMember.equals(familyOfAlbum))
            throw new CustomException(ErrorCode.ALBUM_DELETE_FORBIDDEN);

        albumRepository.deleteById(albumId);
    }

    // 앨범의 사진 조회 (최신순)
    @Transactional(readOnly = true)
    public Page<PhotoInAlbumDTO> getPhotoListByAlbumId(Long memberId, Long albumId, int page, int size) {
        //가족이 맞는지 확인
        Long familyOfMember = memberRepository.findFamilyIdById(memberId)
                .orElseThrow(() -> new CustomException((ErrorCode.FAMILY_NOT_FOUND)));
        Long familyOfAlbum = albumRepository.findFamilyIdById(albumId);

        if(!familyOfMember.equals(familyOfAlbum))
            throw new CustomException(ErrorCode.ALBUM_DELETE_FORBIDDEN);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Photo> photos = photoRepository.findPhotosByAlbumId(albumId, pageable);

        return photos.map(photo -> new PhotoInAlbumDTO(
                photo.getPhoto_id(),
                photo.getUrl()
        ));
    }

    //사진 업로드
    @Transactional
    public void uploadPhoto(Long albumId, Long memberId, String fileName) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new CustomException((ErrorCode.ALBUM_NOT_FOUND)));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Photo photo = Photo.builder()
                .album(album)
                .member(member)
                .url(fileName)
                .build();

        photoRepository.save(photo);
    }

    //사진 상세
    @Transactional(readOnly = true)
    public PhotoRequestDTO getPhotoById(Long memberId, Long photoId) {
        //권한 확인
        Long familyOfPhoto = albumRepository.findFamilyIdById(photoRepository.findAlbumIdByPhotoId(photoId));
        Long familyOfMember = memberRepository.findFamilyIdById(memberId)
                .orElseThrow(() -> new CustomException((ErrorCode.FAMILY_NOT_FOUND)));
        if(!familyOfMember.equals(familyOfPhoto))
            throw new CustomException(ErrorCode.PHOTO_ACCESS_FORBIDDEN);

        Photo photo = photoRepository.findById(photoId).get();
        Long likeCount = likeRepository.countByPhoto(photo);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Boolean isLikedByUser = likeRepository.existsByMemberAndPhoto(member, photo);

        return new PhotoRequestDTO(
                photo.getPhoto_id(),
                photo.getUrl(),
                photo.getCreatedAt(),
                likeCount,
                isLikedByUser
        );
    }

    //사진 삭제
    @Transactional
    public Long deletePhoto(Long memberId, Long photoId) {
        //권한 확인
        Long memberOfPhoto = photoRepository.findMemberById(photoId);
        if(!memberId.equals(memberOfPhoto))
            throw new CustomException(ErrorCode.PHOTO_ACCESS_FORBIDDEN);

        Photo photo = photoRepository.findById(photoId)
                        .orElseThrow(() -> new RuntimeException("사진을 찾을 수 없습니다."));
        Long albumId = photo.getAlbum().getAlbum_id();

        photoRepository.deleteById(photoId);
        return albumId;
    }
}
