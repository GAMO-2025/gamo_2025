package gamo.web.family.controller;

import gamo.web.family.repository.f_MemberRepository;
import gamo.web.family.service.FamilyService;
import gamo.web.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class FamilyController {
    private final FamilyService familyService;
    private final f_MemberRepository memberRepository;

    @GetMapping("/family")
    public String test() {
        Member member = new Member();
        member.setId(10L);

        if(member.getFamily() == null)
            return "non-family";
        else return "has-family";
    }

    @GetMapping("family/new")
    public String createFamily() {
        Member newMember = new Member();
        newMember.setId(10L);

        familyService.create(newMember);

        return "redirect:/family";
    }
}
