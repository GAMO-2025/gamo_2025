package gamo.web.member.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.member.dto.LoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @GetMapping
    public ResponseEntity<LoginResponseDTO> getMyInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        LoginResponseDTO memberResponseDto = new LoginResponseDTO(userPrincipal.getMember());

        return ResponseEntity.ok(memberResponseDto);
    }
}