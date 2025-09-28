package gamo.web.letter.controller;

import gamo.web.letter.Member;
import gamo.web.letter.MemberRepository;
import gamo.web.letter.domain.Letter;
import gamo.web.letter.dto.LetterRequest;
import gamo.web.letter.dto.LetterResponse;
import gamo.web.letter.service.LetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*@RestController
@RequestMapping("/api/letters")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    @PostMapping
    public ResponseEntity<LetterResponse> writeLetter(@RequestBody LetterRequest request) {
        Letter letter = letterService.writeLetter(
                request.getSenderId(),
                request.getReceiverId(),
                request.getTitle(),
                request.getContent(),
                request.getLetterImg(),
                request.getInputType()
        );
        return ResponseEntity.ok(LetterResponse.from(letter));
    }
}*/

@Controller
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;
    private final MemberRepository memberRepository;

    // 편지 작성 화면
    @GetMapping("/letters/new")
    public String showLetterForm(Model model) {
        Long loginMemberId = 1L;
        Member me = memberRepository.findById(loginMemberId).orElseThrow();
        List<Member> familyList = memberRepository.findByFamilyId(me.getFamilyId())
                .stream()
                .filter(m -> !m.getId().equals(loginMemberId))
                .toList();
        model.addAttribute("familyList", familyList);
        return "letterForm";
    }

    // 편지 저장
    @PostMapping("/letters")
    public String submitLetter(@ModelAttribute LetterRequest dto, Model model) {
        Long senderId = 1L;
        Letter letter = letterService.sendLetter(senderId, dto);
        Member receiver = memberRepository.findById(dto.getReceiverId()).orElseThrow();
        model.addAttribute("receiverName", receiver.getName());
        return "letterSuccess";
    }
}



