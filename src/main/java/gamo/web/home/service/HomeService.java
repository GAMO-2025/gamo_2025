package gamo.web.home.service;

import gamo.web.home.dto.HomeSummaryDTO;
import gamo.web.letter.dto.LetterCountDTO;
import gamo.web.letter.service.LetterService;
import gamo.web.member.domain.Member;
import gamo.web.videocall.dto.VideoCallListResponse;
import gamo.web.videocall.dto.VideoCallResponseDTO;
import gamo.web.videocall.service.VideoCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final VideoCallService videoCallService;
    private final LetterService letterService;
    private HomeSummaryDTO hsDTO;

    @Transactional(readOnly = true)
    public HomeSummaryDTO getHomeSummary(Member member) {
        VideoCallListResponse vclr = videoCallService.viewVideoCallHistory(member, 1);
        LetterCountDTO lcDTO = letterService.getLetterCounts(member.getId());

        String targetNickname = null;
        String targetProfileImage = null;

        String ajenda = "추후 추가예정";

        long receivedLetterCount = lcDTO.getReceivedCount();


        // 가장 최근 통화자 정보 가져오기!(1명)
        if (vclr != null && vclr.getContent() != null && !vclr.getContent().isEmpty()) {
            VideoCallResponseDTO latestCall = vclr.getContent().get(0);

            targetNickname = latestCall.getTargetNickName();
            targetProfileImage = latestCall.getTargetProfileImage();
        }

        return HomeSummaryDTO.builder()
                .targetNickname(targetNickname)
                .targetProfileImage(targetProfileImage)
                .ajenda(ajenda)
                .receivedLetterCount(receivedLetterCount)
                .build();

    }
}
