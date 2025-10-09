package gamo.web.auth.handler;

import gamo.web.auth.UserPrincipal;
import gamo.web.auth.jwt.JwtTokenProvider;
import gamo.web.auth.refresh.RefreshToken;
import gamo.web.auth.refresh.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository  refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        Long memberId = ((UserPrincipal) authentication.getPrincipal()).getMember().getId();

        // JWT 생성
        String accessToken = jwtTokenProvider.generateAccessToken(memberId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);

        // refreshToken 만 db 저장
        refreshTokenRepository.save(RefreshToken.builder()
                .memberId(memberId)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(30)) // 프로퍼티로 치환 가능
                .build());

        // 쿠키 생성 및 설정
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true).secure(false).path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true).secure(false).path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // 로그인 성공 시 "/home.html" 페이지로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, "/home");
    }
}