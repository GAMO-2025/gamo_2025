package gamo.web.videocall.controller;
import gamo.web.auth.UserPrincipal;
import gamo.web.family.dto.FamilyListDTO;
import gamo.web.member.service.MemberService;
import gamo.web.videocall.dto.RecommendDTO;
import gamo.web.videocall.dto.VideoCallHistoryListResponse;
import gamo.web.videocall.dto.VideoCallListResponse;
import gamo.web.videocall.service.VideoCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/call")
@RequiredArgsConstructor
@Slf4j
public class VideoCallViewController {

    private final MemberService memberService;
    private final VideoCallService videoCallService;

    /**
     * 영상통화 메인 화면
     * 선택한 사용자의 프로필 상세 조회
     */
    @GetMapping("")
    public String callProfile(
            @AuthenticationPrincipal UserPrincipal user,
            Model model
    ) {
        model.addAttribute("member", user.getMember());
        List<FamilyListDTO> familyList = memberService.getFamilyList(user.getMember().getId());
        VideoCallListResponse response = videoCallService.viewVideoCallHistory(user.getMember(),3);
        model.addAttribute("familyList", familyList);
        model.addAttribute("videoCallHistory",response.getContent());
        return "pages/videocall/callMain";
    }


    // 프로필 조회
    @GetMapping("/video-call/profile")
    public String targetProfile(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam Long partnerId,
            Model model
    ) {
        model.addAttribute("member", user.getMember());
        FamilyListDTO partner = memberService.getFamilyMember(user.getMember().getId(), partnerId);
        model.addAttribute("partner", partner);
        VideoCallHistoryListResponse response = videoCallService.viewVideoCallHistory(user.getMember(), partnerId,5 );
        model.addAttribute("videoCallHistory", response.getContent());
        return "pages/videocall/targetProfile";
    }

    /**
     * 통화 연결 중 화면
     */
    @GetMapping("/loading")
    public String callLoading(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam String mode,
            @RequestParam Long targetId,
            Model model
    ) {
        // mode 검증
        if (!List.of("calling", "incoming", "rejected").contains(mode)) {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }

        Long currentUserId = user.getMember().getId();
        model.addAttribute("mode", mode);
        model.addAttribute("targetId", targetId);
        model.addAttribute("userId", currentUserId);

        try {
            if ("calling".equals(mode)) {
                // 전화 거는 사람 입장
                FamilyListDTO target = memberService.getFamilyMember(currentUserId, targetId);
                if (target == null) {
                    throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
                }
                model.addAttribute("target", target);
                log.info("[CALL] 전화를 걸었습니다. target: {}", target.nickname());

            } else if ("incoming".equals(mode)) {
                // 전화 받는 사람 입장
                FamilyListDTO caller = memberService.getFamilyMember(targetId, currentUserId);
                if (caller == null) {
                    throw new IllegalArgumentException("존재하지 않는 발신자입니다.");
                }
                model.addAttribute("caller", caller);
                log.info("[CALL] 전화가 왔습니다. caller: {}", caller.nickname());
            }
        } catch (Exception e) {
            log.error("[CALL] 통화 연결 실패", e);
            return "redirect:/call";
        }

        return "pages/videocall/loading";
    }

    /**
     * 영상 통화 화면
     */
    @GetMapping("/videocall/{targetId}")
    public String videoCallPage(
            @PathVariable Long targetId,
            @AuthenticationPrincipal UserPrincipal user,
            Model model
    ) {
        FamilyListDTO target = memberService.getFamilyMember(user.getMember().getId(), targetId);
        model.addAttribute("userId", user.getMember().getId());
        model.addAttribute("target", target);
        model.addAttribute("targetId", targetId);
        return "pages/videocall/videocall";
    }




}
