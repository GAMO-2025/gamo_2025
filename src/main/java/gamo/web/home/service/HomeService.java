package gamo.web.home.service;

import gamo.web.home.dto.HomeSummaryDTO;
import gamo.web.letter.dto.LetterCountDTO;
import gamo.web.letter.service.LetterService;
import gamo.web.member.domain.Member;
import gamo.web.photo.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final LetterService letterService;
    private final PhotoService photoService;

    @Transactional(readOnly = true)
    public HomeSummaryDTO getHomeSummary(Member member) {
        LetterCountDTO lcDTO = letterService.getLetterCounts(member.getId());

        String albumThumbnail = null;

        //앨범 최근꺼 하나 가져오기
        if (member.getFamily() != null) {
            Long familyId = member.getFamily().getId();
            albumThumbnail = photoService.getLatestAlbumThumbnailByFamily(familyId);
        }


        long unreadLetterCount = lcDTO.getUnreadCount();

        return HomeSummaryDTO.builder()
                .AlbumThumbnail(albumThumbnail)
                .unreadLetterCount(unreadLetterCount)
                .build();

    }
}
