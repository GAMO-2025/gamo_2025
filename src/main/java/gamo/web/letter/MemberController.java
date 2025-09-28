package gamo.web.letter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/family/{memberId}")
    public ResponseEntity<List<MemberDto>> getFamilyMembers(@PathVariable Long memberId) {
        Member me = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 회원"));

        List<Member> familyMembers = memberRepository.findByFamilyId(me.getFamilyId());

        List<MemberDto> result = familyMembers.stream()
                .filter(m -> !m.getId().equals(memberId)) // 나 자신 제외
                .map(MemberDto::from)
                .toList();

        return ResponseEntity.ok(result);
    }
}

