package gamo.web.letter.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.letter.service.LetterSseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class LetterSseController {

    private final LetterSseEmitters emitters;

    @GetMapping("/letters")
    public SseEmitter subscribe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            // 로그인 안 된 사용자는 SSE 연결 차단
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        Long memberId = userPrincipal.getMember().getId();
        return emitters.subscribe(memberId);
    }
}

