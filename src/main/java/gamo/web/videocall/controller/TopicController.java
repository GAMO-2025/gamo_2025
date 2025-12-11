package gamo.web.videocall.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.videocall.dto.RecommendDTO;
import gamo.web.videocall.dto.VideoCallHistoryDTO;
import gamo.web.videocall.dto.VideoCallResponseDTO;
import gamo.web.videocall.service.TopicService;
import gamo.web.videocall.service.VideoCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/topics")
@RestController
public class TopicController {

    private final VideoCallService videoCallService;
    private final TopicService topicService;

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

    @PostMapping("/proxy/{userId}")
    public ResponseEntity<String> getProxyTopic(@RequestBody RecommendDTO.TopicProxyRequest request, @PathVariable Long userId) {
        videoCallService.resolveTopicWithRecentCallId(request, userId);
        return ResponseEntity.ok().build();
    }

}
