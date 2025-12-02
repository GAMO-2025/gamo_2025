package gamo.web.videocall.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.videocall.dto.RecommendDTO;
import gamo.web.videocall.service.VideoCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/topics")
@RestController
public class TopicController {

    private final VideoCallService videoCallService;

    @GetMapping("/recommended/{targetId}")
    public RecommendDTO.KeywordResponse getRecommendedTopic
            (@AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long targetId) {
        return videoCallService.viewRecommendedKeywords(user.getMember(), targetId, 3);
    }

    @GetMapping("/home")
    public RecommendDTO.HomeKeywordResponseDTO getHomeTopic(@AuthenticationPrincipal UserPrincipal user) {
        return videoCallService.viewLatestRecommendedKeywords(user.getMember());
    }

}
