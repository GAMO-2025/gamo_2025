package gamo.web.letter.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.family.dto.FamilyListDTO;
import gamo.web.letter.domain.Letter;
import gamo.web.letter.dto.*;
import gamo.web.letter.service.LetterService;
import gamo.web.member.domain.Member;
import gamo.web.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LetterViewController {

    private final LetterService letterService;
    private final MemberService memberService;
    private static final int PAGE_SIZE = 3; // 페이지당 편지 개수

    // -------------------------------
    // 편지 홈
    // -------------------------------
    @GetMapping("/letter")
    public String showLetterHome(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        Member loginMember = userPrincipal.getMember();
        Long loginMemberId = loginMember.getId();

        LetterCountDTO letterCounts = letterService.getLetterCounts(loginMemberId);
        model.addAttribute("letterCounts", letterCounts);

        return "/pages/letter/letterHome";
    }

    // -------------------------------
    // 편지 목록
    // -------------------------------
    @GetMapping("/letter/list")
    public String showLetterList(
            @RequestParam(defaultValue = "received") String type,
            @RequestParam(required = false) Long personId,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Model model) {

        Member loginMember = userPrincipal.getMember();
        Long loginMemberId = loginMember.getId();

        boolean received = "received".equals(type);

        // 편지 목록 조회
        Page<LetterListDTO> letterPage = letterService.getLetters(loginMemberId, personId, received, sort, page, PAGE_SIZE);

        // 사람 목록 조회 (발신자/수신자)
        List<PersonDTO> personList = letterService.getLetterPeople(loginMemberId, received);

        model.addAttribute("letters", letterPage.getContent());
        model.addAttribute("personList", personList);
        model.addAttribute("type", type);
        model.addAttribute("personId", personId);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", letterPage.getTotalPages());

        return "/pages/letter/letterList";
    }

    // -------------------------------
    // 편지 작성 폼
    // -------------------------------
    @GetMapping("/letter/new")
    public String showLetterForm(
            @RequestParam(required = false) Long receiverId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Model model) {

        Member loginMember = userPrincipal.getMember();
        Long loginMemberId = loginMember.getId();

        // 가족 목록 조회
        List<FamilyListDTO> familyList = memberService.getFamilyList(loginMemberId);
        model.addAttribute("familyList", familyList);

        // 답장 대상 미리 선택
        FamilyListDTO preSelectedReceiver = null;
        if (receiverId != null) {
            preSelectedReceiver = familyList.stream()
                    .filter(f -> f.id().equals(receiverId))
                    .findFirst()
                    .orElse(null);
        }
        model.addAttribute("preSelectedReceiver", preSelectedReceiver);

        return "/pages/letter/letterForm";
    }

    // -------------------------------
    // 편지 전송
    // -------------------------------
    @PostMapping("/letter/send")
    public String submitLetter(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute LetterRequestDTO letterRequest,
            Model model) {

        Member loginMember = userPrincipal.getMember();
        Long senderId = loginMember.getId();

        LetterResponseDTO response = letterService.sendLetter(senderId, letterRequest);

        String receiverName = letterService.getDisplayName(senderId, letterRequest.getReceiverId());
        model.addAttribute("receiverName", receiverName);
        model.addAttribute("letterId", response.getLetterId());

        return "/pages/letter/letterSuccess";
    }

    // -------------------------------
    // 편지 삭제
    // -------------------------------
    @PostMapping("/letter/delete/{letterId}")
    @ResponseBody
    public String deleteLetter(
            @PathVariable Long letterId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getMember().getId();
        try {
            letterService.deleteLetter(letterId, userId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    // -------------------------------
    // 편지 전송 취소
    // -------------------------------
    @PostMapping("/letter/cancel")
    public String cancelLetter(@RequestParam Long letterId) {
        letterService.cancelLetter(letterId);
        return "redirect:/letter/list?type=sent";
    }

    // -------------------------------
    // 편지 상세 조회
    // -------------------------------
    @GetMapping("/letter/detail/{letterId}")
    public String showLetterDetail(
            @PathVariable Long letterId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Model model) {

        Member loginMember = userPrincipal.getMember();
        Long loginMemberId = loginMember.getId();

        try {
            LetterDetailDTO letterDetail = letterService.getLetterDetail(letterId, loginMemberId);
            model.addAttribute("letter", letterDetail);
            return "/pages/letter/letterDetail";
        } catch (Exception e) {
            // 권한 없거나 편지가 없는 경우
            return "redirect:/letter/list";
        }
    }
}
