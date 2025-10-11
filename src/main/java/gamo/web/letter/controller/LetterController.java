package gamo.web.letter.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.letter.domain.Letter;
import gamo.web.letter.dto.LetterRequestDTO;
import gamo.web.letter.service.LetterService;
import gamo.web.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    // 편지 작성
    @GetMapping("/letter/new")
    public String showLetterForm(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        Member loginMember = userPrincipal.getMember();
        Long loginMemberId = loginMember.getId();

        List<LetterService.FamilyDisplay> familyDisplayList = letterService.getFamilyDisplayList(loginMemberId);
        model.addAttribute("familyList", familyDisplayList);
        return "/pages/letter/letterForm";
    }

    // 편지 전송(저장)
    @PostMapping("/letter/send")
    public String submitLetter(@AuthenticationPrincipal UserPrincipal userPrincipal, @ModelAttribute LetterRequestDTO letterRequest, Model model) {
        // 로그인 한 회원 정보 가져오기
        Member loginMember = userPrincipal.getMember();
        Long senderId = loginMember.getId();

        // 편지 전송
        Letter letter = letterService.sendLetter(senderId, letterRequest);

        // 수신자 표시 이름 가져오기
        String receiverName = letterService.getReceiverDisplayName(senderId, letterRequest.getReceiverId());
        model.addAttribute("receiverName", receiverName);
        model.addAttribute("letterId", letter.getId());

        return "/pages/letter/letterSuccess";
    }

    // 편지 전송 취소
    @PostMapping("/letter/cancel")
    public String cancelLetter(@RequestParam Long letterId) {
        letterService.cancelLetter(letterId);
        return "redirect:/letter/new";
    }

    // 편지 홈(임시)
    @GetMapping("/letter")
    public String showLetterHome() {
        return "/pages/letter/letterHome";
    }
}