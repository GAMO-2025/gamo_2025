package gamo.web.home.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.member.dto.LoginResponseDTO;
import gamo.web.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalMemberAdvice {

    private final MemberService memberService;

    @ModelAttribute("member")
    public LoginResponseDTO addLoggedInMember(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null) return null;
        Long memberId = memberService.getMemberId(user);
        return memberService.getMyInfo(memberId);
    }
}