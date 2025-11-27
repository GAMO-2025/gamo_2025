package gamo.web.home.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.member.domain.Member;
import gamo.web.member.dto.LoginResponseDTO;
import gamo.web.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalMemberAdvice {

    @ModelAttribute("member")
    public LoginResponseDTO addLoggedInMember(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null) return null;

        // DB 호출 없이 UserPrincipal에 있는 Member 사용
        Member member = user.getMember();
        return new LoginResponseDTO(member);
    }
}