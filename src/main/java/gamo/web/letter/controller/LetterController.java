package gamo.web.letter.controller;

import gamo.web.letter.Nickname;
import gamo.web.letter.NicknameRepository;
import gamo.web.letter.domain.Letter;
import gamo.web.letter.dto.LetterRequest;
import gamo.web.letter.service.LetterService;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;
    private final MemberRepository memberRepository;
    private final NicknameRepository nicknameRepository;

    // 편지 작성 화면
    @GetMapping("/letters/new")
    public String showLetterForm(Model model) {
        Long loginMemberId = 1L; // 테스트용 로그인 ID
        Optional<Member> optionalMe = memberRepository.findById(loginMemberId);

        if (optionalMe.isEmpty()) {
            model.addAttribute("errorMessage", "로그인 회원이 존재하지 않습니다.");
            return "errorPage"; // errorPage.html 화면으로 이동
        }

        Member me = optionalMe.get();

        // 같은 가족원 목록 (나 제외)
        List<Member> familyList = memberRepository.findByFamily(me.getFamily())
                .stream()
                .filter(m -> !m.getId().equals(loginMemberId))
                .toList();

        // 가족원 표시용 DTO 생성 (nickname 있으면 alias, 없으면 "닉네임이 없습니다")
        List<FamilyDisplay> familyDisplayList = familyList.stream()
                .map(member -> {
                    String displayName = nicknameRepository.findByUserIdAndAliasUserId(loginMemberId, member.getId())
                            .map(Nickname::getAlias)
                            .orElse("닉네임이 없습니다");
                    return new FamilyDisplay(member.getId(), displayName);
                })
                .toList();

        model.addAttribute("familyList", familyDisplayList);
        return "letterForm";
    }



    // 편지 저장
    @PostMapping("/letters")
    public String submitLetter(@ModelAttribute LetterRequest dto, Model model) {
        Long senderId = 1L;

        // 편지 전송
        Letter letter = letterService.sendLetter(senderId, dto);

        // 수신자 조회
        Member receiver = memberRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수신자입니다."));

        // 수신자 표시 이름 조회
        String receiverName = nicknameRepository.findByUserIdAndAliasUserId(senderId, receiver.getId())
                .map(Nickname::getAlias)  // 닉네임 있으면 사용
                .orElse(receiver.getName()); // 없으면 Member name 사용

        model.addAttribute("receiverName", receiverName);

        return "letterSuccess";
    }


    // 화면에 뿌려줄 DTO
    record FamilyDisplay(Long id, String displayName) {}
}




