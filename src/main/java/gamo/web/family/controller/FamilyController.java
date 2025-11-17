package gamo.web.family.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.family.service.FamilyService;
import gamo.web.member.service.MemberService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class FamilyController {
    private final FamilyService familyService;
    private final MemberService memberService;

    @PostMapping("/invite")
    public ResponseEntity<Map<String, String>> inviteFamily(@AuthenticationPrincipal UserPrincipal user) {
        Long memberId = memberService.getMemberId(user);
       if(familyService.hasFamily(memberId)) {
           String code =  familyService.getFamilyCode(memberId);
           return ResponseEntity.ok().body(Map.of("familyCode", code));
       }

       familyService.inviteFamily(memberId);
       String code =  familyService.getFamilyCode(memberId);
       return ResponseEntity.ok().body(Map.of("familyCode", code));
    }

    @Getter
    @Setter
    public static class InviteCode {
        private String inviteCode;
    }

    @PostMapping("/join")
    public ResponseEntity<Void> joinFamily(@AuthenticationPrincipal UserPrincipal user,
                                           @RequestBody InviteCode inviteCode) {
        Long memberId = memberService.getMemberId(user);
        familyService.joinFamily(memberId, inviteCode.getInviteCode());
        return ResponseEntity.ok().build();
    }
}
