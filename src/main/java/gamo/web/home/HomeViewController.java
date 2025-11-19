package gamo.web.home;

import gamo.web.auth.UserPrincipal;
import gamo.web.member.dto.LoginResponseDTO;
import gamo.web.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeViewController {

    private final MemberService memberService;

    @ModelAttribute("member")
    public LoginResponseDTO currentMember(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null) return null;               // 비로그인 대비
        Long memberId = memberService.getMemberId(user);
        return memberService.getMyInfo(memberId);
    }


}
