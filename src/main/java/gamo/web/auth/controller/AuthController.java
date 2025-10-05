package gamo.web.auth.controller;

import gamo.web.auth.jwt.JwtTokenProvider;
import gamo.web.auth.refresh.RefreshToken;
import gamo.web.auth.refresh.RefreshTokenRepository;
import gamo.web.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 한국 시간대
    ZoneId KST = ZoneId.of("Asia/Seoul");


    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = Arrays.stream(request.getCookies() != null ? request.getCookies() : new jakarta.servlet.http.Cookie[0])
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .filter(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now(KST)))
                .orElse(null);

        if (stored == null) {
            return ResponseEntity.status(401).build();
        }

        Long memberId = tokenProvider.getMemberIdFromToken(refreshToken);

        // Access 재발급
        String newAccess = tokenProvider.generateAccessToken(memberId);

        refreshTokenRepository.save(stored);

        String newRefresh = tokenProvider.generateRefreshToken(memberId);
        refreshTokenRepository.save(RefreshToken.builder()
                .memberId(memberId)
                .token(newRefresh)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build());

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccess)
                .httpOnly(true).secure(false).path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefresh)
                .httpOnly(true).secure(false).path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok().build();
    }
}
