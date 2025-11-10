package gamo.web.member.controller;

import gamo.web.member.domain.Member;
import gamo.web.member.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class FamilyController {
    private final FamilyService familyService;

    @GetMapping("/family") //초기화면: 가족초대 버튼 클릭하면 뜨는거
    public String join() {
        Member member = new Member(); //임시
//        Family family = new Family();
//        member.setFamily(family);

        if(member.getFamily() == null)
            return "/pages/family/non-family";
        return "/pages/family/has-family";
    }

    @PostMapping("/family/create")
    public String createFamily() {
//        Member member = new Member(); //임시
//        member.setId(1L);

        if(familyService.isFamilyEmpty(1L)) {
            familyService.createFamily(1L);
        }
        return "redirect:/family/code";
    }

    @GetMapping("/family/code") //가족코드 보기
    public String showFamilyCode(Model model) {
        String familyCode = familyService.getFamilyCode(1L); //임시
        model.addAttribute("familyCode", familyCode);
        return "/pages/family/family-code";
    }

    @PostMapping("/family/join")
    public String joinFamily(@RequestParam("inviteCode") String inviteCode) {
        boolean success = familyService.joinFamily(inviteCode, 2L); //임시 memberid

        if(!success) {
            return "redirect:/family/error";
        }
        return "redirect:/family";
    }
}
