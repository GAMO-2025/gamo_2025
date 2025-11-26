package gamo.web.photo.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.member.repository.MemberRepository;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.repository.PhotoRepository;
import gamo.web.photo.service.GcpStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class PhotoProxyController {

    private final GcpStorageService gcpStorageService;
    private final PhotoRepository photoRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/photo/file/{fileName}")
    public ResponseEntity<byte[]> getPhoto(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String fileName) throws IOException {

        // 1) 로그인 체크
        if (user == null) {
            throw new CustomException(ErrorCode.PHOTO_ACCESS_FORBIDDEN);
        }
        Long memberId = user.getMember().getId();

        // 2) 파일명으로 Photo 조회
        Photo photo = photoRepository.findByUrl(fileName);
        if (photo == null) {
            throw new CustomException(ErrorCode.PHOTO_NOT_FOUND);
        }

        // 3) 가족 권한 체크
        Long familyOfPhoto = photo.getAlbum().getFamily().getId();
        Long familyOfMember = memberRepository.findFamilyIdById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.FAMILY_NOT_FOUND));

        if (!familyOfPhoto.equals(familyOfMember)) {
            throw new CustomException(ErrorCode.PHOTO_ACCESS_FORBIDDEN);
        }

        // 4) GCS 에서 바이트 가져오기
        byte[] bytes = gcpStorageService.load(fileName);

        // 5) Content-Type 세팅 (대부분 이미지라 일단 jpeg로, 필요하면 Photo에 contentType 필드 추가)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "image/jpeg");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}
