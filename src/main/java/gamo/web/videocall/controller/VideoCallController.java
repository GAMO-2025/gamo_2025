package gamo.web.videocall.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.common.response.ApiResponse;
import gamo.web.common.response.SuccessCode;
import gamo.web.videocall.dto.VideoCallListResponse;
import gamo.web.videocall.service.VideoCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/api/video-calls")
@RequiredArgsConstructor
public class VideoCallController {

    private final VideoCallService videoCallService;

    /**
     * 통화 내역 최신순 조회
     */
    @GetMapping("/{userId}")
    public ApiResponse<VideoCallListResponse> getVideoCallHistory(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "10") int size
    ) {
        VideoCallListResponse result =
                videoCallService.viewVideoCallHistory(user.getMember(), size);
        return ApiResponse.onSuccess(result, SuccessCode.OK);
    }

    /**
     * 가장 최근의 통화 기록 조회
     */
    @GetMapping("/latest/{userId}")
    public ResponseEntity<Long> getLatestCallId(
            @PathVariable Long userId
    ) {
        Long latestCallId = videoCallService.getLatestCallId(userId);
        return ResponseEntity.ok(latestCallId);
    }

}
