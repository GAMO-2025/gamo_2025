package gamo.web.letter.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.letter.domain.Letter;
import gamo.web.letter.dto.*;
import gamo.web.letter.service.LetterService;
import gamo.web.member.domain.Member;
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

    private static final int PAGE_SIZE = 3; // 페이지당 편지 개수

    // 편지 보관함(목록)
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

        Page<LetterListDTO> letterPage;
        List<PersonDTO> personList;

        if ("received".equals(type)) {
            letterPage = letterService.getReceivedLetters(loginMemberId, personId, sort, page, PAGE_SIZE);
            personList = letterService.getReceivedLetterSenders(loginMemberId);
        } else {
            letterPage = letterService.getSentLetters(loginMemberId, personId, sort, page, PAGE_SIZE);
            personList = letterService.getSentLetterReceivers(loginMemberId);
        }

        model.addAttribute("letters", letterPage.getContent());
        model.addAttribute("personList", personList);
        model.addAttribute("type", type);
        model.addAttribute("personId", personId);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", letterPage.getTotalPages());

        return "/pages/letter/letterList";
    }

    // 편지 작성
    @GetMapping("/letter/new")
    public String showLetterForm(
            @RequestParam(required = false) Long receiverId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Model model) {

        Member loginMember = userPrincipal.getMember();
        Long loginMemberId = loginMember.getId();

        // 로그인한 사용자의 가족 목록 조회
        List<LetterService.FamilyDisplay> familyDisplayList = letterService.getFamilyDisplayList(loginMemberId);
        model.addAttribute("familyList", familyDisplayList);

        // 답장하기 시 수신자 미리 선택
        LetterService.FamilyDisplay preSelectedReceiver = null;
        if (receiverId != null) {
            preSelectedReceiver = familyDisplayList.stream()
                    .filter(f -> f.id().equals(receiverId))
                    .findFirst()
                    .orElse(null);
        }

        model.addAttribute("preSelectedReceiver", preSelectedReceiver);

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
        return "redirect:/letter/list?type=sent";
    }

    // 편지 삭제
    @PostMapping("/letter/delete/{letterId}")
    @ResponseBody
    public String deleteLetter(
            @PathVariable Long letterId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Long userId = userPrincipal.getMember().getId();
            letterService.deleteLetter(letterId, userId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    // 편지 홈
    @GetMapping("/letter")
    public String showLetterHome(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        Member loginMember = userPrincipal.getMember();
        Long loginMemberId = loginMember.getId();

        // 편지 개수 조회
        LetterCountDTO letterCounts = letterService.getLetterCounts(loginMemberId);
        model.addAttribute("letterCounts", letterCounts);

        return "/pages/letter/letterHome";
    }

    // 편지 상세 조회
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
        } catch (IllegalArgumentException e) {
            // 권한 없거나 편지가 없는 경우
            return "redirect:/letter/list";
        }
    }
}