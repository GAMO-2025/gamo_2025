package gamo.web.auth.controller;

import gamo.web.auth.jwt.JwtTokenProvider;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Profile("local")  // 로컬 전용!!! 테스트 하기 위해 있는거임
@Controller
@RequestMapping("/test-auth")
@RequiredArgsConstructor
public class TestAuthController {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.access-token-name:accessToken}")
    private String accessTokenName;

    @Value("${jwt.refresh-token-name:refreshToken}")
    private String refreshTokenName;

    @GetMapping("/login/{memberId}")
    public String loginAndGoHome(
            @PathVariable Long memberId,
            HttpServletResponse response
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 memberId: " + memberId));

        String accessToken = jwtTokenProvider.generateAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId());

        ResponseCookie accessCookie = ResponseCookie.from(accessTokenName, accessToken)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(refreshTokenName, refreshToken)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // ✅ 여기서 네가 원하는 페이지로 리다이렉트
        // 예: 홈 화면
        return "redirect:/home";
        // 예: 편지 작성 페이지가 /letter/write 라면
        // return "redirect:/letter/write";
    }
}
