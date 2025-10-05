package gamo.web.letter.controller;

import gamo.web.letter.domain.Letter;
import gamo.web.letter.dto.LetterRequest;
import gamo.web.letter.repository.LetterRepository;
import gamo.web.letter.service.LetterService;
import gamo.web.member.domain.Member;
import gamo.web.member.domain.Nickname;
import gamo.web.member.repository.MemberRepository;
import gamo.web.member.repository.NicknameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    // 편지 작성 화면
    @GetMapping("/letters/new")
    public String showLetterForm(Model model) {
        Long loginMemberId = 1L; // 테스트용 로그인 ID
        List<LetterService.FamilyDisplay> familyDisplayList = letterService.getFamilyDisplayList(loginMemberId);
        model.addAttribute("familyList", familyDisplayList);
        return "letterForm";
    }

    // 편지 전송(저장)
    @PostMapping("/letters/send")
    public String submitLetter(@ModelAttribute LetterRequest dto, Model model) {
        Long senderId = 1L;
        Letter letter = letterService.sendLetter(senderId, dto);

        String receiverName = letterService.getReceiverDisplayName(senderId, dto.getReceiverId());
        model.addAttribute("receiverName", receiverName);
        model.addAttribute("letterId", letter.getId());

        return "letterSuccess";
    }

    // 편지 전송 취소
    @PostMapping("/letters/cancel")
    public String cancelLetter(@RequestParam Long letterId) {
        letterService.cancelLetter(letterId);
        return "redirect:/letters/new";
    }
}





