package gamo.web.family.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.family.service.FamilyService;
import gamo.web.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
