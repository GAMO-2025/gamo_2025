package gamo.web.member.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.member.domain.Member;
import gamo.web.member.dto.LoginResponseDTO;
import gamo.web.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberViewController {

    private final MemberService memberService;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserPrincipal user, Model model) {
        Long memberId = memberService.getMemberId(user);
        LoginResponseDTO member = memberService.getMyInfo(memberId);
        model.addAttribute("member", member); // 타임리프에서 ${member.*}로 접근
        return "member/mypage";
    }
}