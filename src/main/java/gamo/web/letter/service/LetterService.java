package gamo.web.letter.service;

import gamo.web.letter.domain.InputType;
import gamo.web.letter.domain.Letter;
import gamo.web.letter.dto.LetterRequestDTO;
import gamo.web.letter.repository.LetterRepository;
import gamo.web.member.domain.Member;
import gamo.web.member.domain.Nickname;
import gamo.web.member.repository.MemberRepository;
import gamo.web.member.repository.NicknameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;
    private final NicknameRepository nicknameRepository;
    private final SttService sttService;
    private final GcsService gcsService;
    private final AiCorrectService aiCorrectService;

    // 편지 작성 화면용 가족 목록
    public List<FamilyDisplay> getFamilyDisplayList(Long loginMemberId) {
        Member me = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 회원이 존재하지 않습니다."));

        return memberRepository.findByFamily(me.getFamily())
                .stream()
                .filter(m -> !m.getId().equals(loginMemberId))
                .map(member -> {
                    String displayName = nicknameRepository
                            .findByMemberIdAndAliasMemberId(loginMemberId, member.getId())
                            .map(Nickname::getAlias)
                            .orElse(member.getName());
                    return new FamilyDisplay(member.getId(), displayName);
                })
                .toList();
    }

    // 수신자 표시 이름
    public String getReceiverDisplayName(Long senderId, Long receiverId) {
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수신자입니다."));
        return nicknameRepository.findByMemberIdAndAliasMemberId(senderId, receiver.getId())
                .map(Nickname::getAlias)
                .orElse(receiver.getName());
    }

    // 편지 전송
    public Letter sendLetter(Long senderId, LetterRequestDTO request) {
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 발신자 ID"));
        Member receiver = memberRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 수신자 ID"));

        if (!sender.getFamily().getId().equals(receiver.getFamily().getId())) {
            throw new IllegalArgumentException("같은 가족이 아닙니다.");
        }

        // 기본 content
        String content = request.getContent();

        // 이미지 업로드
        String letterImgPath = null;
        if (request.getLetterImg() != null && !request.getLetterImg().isEmpty()) {
            letterImgPath = gcsService.uploadFile(request.getLetterImg());
        }

        // DB 저장
        Letter letter = Letter.builder()
                .senderId(senderId)
                .receiverId(receiver.getId())
                .title(request.getTitle())
                .content(content)
                .letterImg(letterImgPath)
                .inputType(InputType.valueOf(request.getInputType()))
                .build();

        return letterRepository.save(letter);
    }

    // 편지 취소
    @Transactional
    public void cancelLetter(Long letterId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new IllegalArgumentException("편지가 존재하지 않습니다."));
        letter.setCancelled(true);
    }

    // 화면에 뿌릴 DTO
    public record FamilyDisplay(Long id, String displayName) {}
}
