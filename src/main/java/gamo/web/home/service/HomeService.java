package gamo.web.home.service;

import gamo.web.home.dto.HomeSummaryDTO;
import gamo.web.letter.dto.LetterCountDTO;
import gamo.web.letter.service.LetterService;
import gamo.web.member.domain.Member;
import gamo.web.photo.service.PhotoService;
import gamo.web.videocall.dto.RecommendDTO;
import gamo.web.videocall.service.VideoCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final VideoCallService videoCallService;
    private final LetterService letterService;
    private final PhotoService photoService;


    @Transactional(readOnly = true)
    public HomeSummaryDTO getHomeSummary(Member member) {

        LetterCountDTO lcDTO = letterService.getLetterCounts(member.getId());
        long unreadLetterCount = lcDTO.getUnreadCount();

        String targetNickname = null;
        String targetProfileImage = null;

        String albumThumbnail = null;

        //앨범 최근꺼 하나 가져오기
        if (member.getFamily() != null) {
            Long familyId = member.getFamily().getId();
            albumThumbnail = photoService.getLatestAlbumThumbnailByFamily(familyId);
        }

        String ajenda = "추후 추가예정";




        RecommendDTO.HomeKeywordResponseDTO homeKeyword =
                videoCallService.viewLatestRecommendedKeywords(member);

        if (homeKeyword != null) {
            targetNickname = homeKeyword.getName();
            targetProfileImage = homeKeyword.getProfileImage();
            ajenda = homeKeyword.getTopic();
        }


        return HomeSummaryDTO.builder()
                .targetNickname(targetNickname)
                .targetProfileImage(targetProfileImage)
                .AlbumThumbnail(albumThumbnail)
                .ajenda(ajenda)
                .unreadLetterCount(unreadLetterCount)
                .build();

    }
}
