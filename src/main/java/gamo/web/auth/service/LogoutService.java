package gamo.web.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    public void performLogout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 세션/시큐리티 컨텍스트 정리
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, auth);
        SecurityContextHolder.clearContext();

        // 2. JWT 쿠키 만료
        ResponseCookie expiredAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(false).path("/")
                .maxAge(0).sameSite("Lax").build();

        ResponseCookie expiredRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(false).path("/")
                .maxAge(0).sameSite("Lax").build();

        response.addHeader("Set-Cookie", expiredAccess.toString());
        response.addHeader("Set-Cookie", expiredRefresh.toString());
    }
}
