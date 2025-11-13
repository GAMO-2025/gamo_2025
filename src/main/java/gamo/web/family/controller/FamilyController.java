package gamo.web.family.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.family.service.FamilyService;
import gamo.web.member.service.MemberService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
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
        // 이미 가족에 가입되어 있는 사람이라면, 그 가족의 코드를 보여준다
        if(familyService.hasFamily(memberId)) {
           String code =  familyService.getFamilyCode(memberId);
           return ResponseEntity.ok().body(Map.of("familyCode", code));
        }

        // 아니라고 하면, 코드 새로 생성해서 그걸 보내줌
        familyService.inviteFamily(memberId);
        String code =  familyService.getFamilyCode(memberId);
        return ResponseEntity.ok().body(Map.of("familyCode", code));
    }

    @Getter
    @Setter
    public static class InviteCode {
        private String inviteCode;
    }

    // 가족 코드 입력해서 그 가족에 들어가기(초대받기)
    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> joinFamily(@AuthenticationPrincipal UserPrincipal user,
                                           @RequestBody InviteCode inviteCode) {
        Long memberId = memberService.getMemberId(user);

        if (familyService.hasFamily(memberId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 가입된 가족이 있습니다."));
        }
        familyService.joinFamily(memberId, inviteCode.getInviteCode());
        return ResponseEntity.ok(Map.of("message", "가족 참여 완료!"));
    }
}
