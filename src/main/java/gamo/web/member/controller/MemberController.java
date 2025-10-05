package gamo.web.member.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.auth.jwt.JwtTokenProvider;
import gamo.web.member.domain.Member;
import gamo.web.member.dto.LoginResponseDTO;
import gamo.web.member.dto.NicknameRequestDTO;
import gamo.web.member.repository.MemberRepository;
import gamo.web.member.service.MemberService;
import gamo.web.member.service.NicknameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final NicknameService nicknameService;

    //dev login 용 나중에 삭제
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/dev/login")
    public ResponseEntity<String> devLogin(Long memberId) {
        Member m = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Member not found"));

        String token = jwtTokenProvider.generateAccessToken(m.getId());

        return ResponseEntity.ok(token);
    }

    @GetMapping
    public ResponseEntity<LoginResponseDTO> getMyInfo(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Long memberId = memberService.getMemberId(user);

        LoginResponseDTO response = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{aliasMemberId}")
    public ResponseEntity<Void> upsertNickname(
            @PathVariable Long aliasMemberId,
            @Valid @RequestBody NicknameRequestDTO nicknameRequestDTO,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Long memberId = memberService.getMemberId(user);
        nicknameService.upsertNickname(memberId, aliasMemberId, nicknameRequestDTO.getAlias());
        return ResponseEntity.ok().build();
    }
}