package gamo.web.member.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.member.dto.FamilyListDTO;
import gamo.web.member.dto.LoginResponseDTO;
import gamo.web.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberViewController {

    private final MemberService memberService;

    @ModelAttribute("member")
    public LoginResponseDTO currentMember(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null) return null;               // 비로그인 대비
        Long memberId = memberService.getMemberId(user);
        return memberService.getMyInfo(memberId);
    }

    @GetMapping("/family")
    public String family(@AuthenticationPrincipal UserPrincipal user, Model model) {
        if(user == null) {
            return "redirect:/login";
        }
        Long memberId = memberService.getMemberId(user);

        List<FamilyListDTO> familyList = memberService.getFamilyList(memberId);

        model.addAttribute("familyList", familyList);

        return "pages/member/family";
    }

    @GetMapping("/invite")
    public String invite() {
        return "pages/member/invite";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "pages/member/mypage";
    }
}