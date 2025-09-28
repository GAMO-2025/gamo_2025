package gamo.web.auth.jwt;

import gamo.web.auth.UserPrincipal;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. 요청 쿠키에서 토큰 추출
            String token = getTokenFromCookie(request);
            log.info("1. Extracted Token from Cookie: {}", token);

            // 2. 토큰이 존재하고 유효하다면
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                log.info("2. Token validation successful.");
                // 3. 토큰에서 회원 ID 추출
                Long memberId = jwtTokenProvider.getMemberIdFromToken(token);

                log.info("3. Extracted Member ID from Token: {}", memberId);

                // 4. 회원 ID로 DB에서 회원 정보 조회
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ServletException("User not found"));
                log.info("4. Member found in DB: {}", member.getName());

                // 5. UserPrincipal 객체 생성 (인증 객체에 담을 정보)
                UserPrincipal userPrincipal = new UserPrincipal(member, null);

                // 6. 인증(Authentication) 객체 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. SecurityContext에 인증 객체 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("5. Authentication successful. SecurityContext updated for member ID: {}", memberId);
            } else {
                log.warn("2. Token is null or invalid.");
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
