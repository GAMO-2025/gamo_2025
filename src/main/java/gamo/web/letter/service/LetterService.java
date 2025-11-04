package gamo.web.letter.service;

import gamo.web.letter.domain.InputType;
import gamo.web.letter.domain.Letter;
import gamo.web.letter.dto.LetterCountDTO;
import gamo.web.letter.dto.LetterListDTO;
import gamo.web.letter.dto.LetterRequestDTO;
import gamo.web.letter.dto.PersonDTO;
import gamo.web.letter.repository.LetterRepository;
import gamo.web.member.domain.Member;
import gamo.web.member.domain.Nickname;
import gamo.web.member.repository.MemberRepository;
import gamo.web.member.repository.NicknameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;
    private final NicknameRepository nicknameRepository;
    private final SttService sttService;
    private final GcsService gcsService;
    private final AiCorrectService aiCorrectService;

    // 편지 개수 조회
    public LetterCountDTO getLetterCounts(Long userId) {
        Long receivedCount = letterRepository.countByReceiverId(userId);
        Long sentCount = letterRepository.countBySenderId(userId);
        Long unreadCount = letterRepository.countByReceiverIdAndIsReadFalse(userId);

        return new LetterCountDTO(receivedCount, sentCount, unreadCount);
    }

    // 받은 편지 목록 조회
    public Page<LetterListDTO> getReceivedLetters(Long userId, Long senderId, String sort, int page, int size) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Page<Letter> letterPage;
        if (senderId != null) {
            letterPage = letterRepository.findByReceiverIdAndSenderId(userId, senderId, pageable);
        } else {
            letterPage = letterRepository.findByReceiverId(userId, pageable);
        }

        return letterPage.map(letter -> {
            String senderName = getDisplayName(userId, letter.getSenderId());
            return LetterListDTO.fromReceivedLetter(letter, senderName);
        });
    }

    // 보낸 편지 목록 조회
    public Page<LetterListDTO> getSentLetters(Long userId, Long receiverId, String sort, int page, int size) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Page<Letter> letterPage;
        if (receiverId != null) {
            letterPage = letterRepository.findBySenderIdAndReceiverId(userId, receiverId, pageable);
        } else {
            letterPage = letterRepository.findBySenderId(userId, pageable);
        }

        return letterPage.map(letter -> {
            String receiverName = getDisplayName(userId, letter.getReceiverId());
            return LetterListDTO.fromSentLetter(letter, receiverName);
        });
    }

    // 받은 편지의 발신자 목록
    public List<PersonDTO> getReceivedLetterSenders(Long userId) {
        List<Long> senderIds = letterRepository.findDistinctSenderIdsByReceiverId(userId);
        return senderIds.stream()
                .map(senderId -> {
                    String name = getDisplayName(userId, senderId);
                    return new PersonDTO(senderId, name);
                })
                .collect(Collectors.toList());
    }

    // 보낸 편지의 수신자 목록
    public List<PersonDTO> getSentLetterReceivers(Long userId) {
        List<Long> receiverIds = letterRepository.findDistinctReceiverIdsBySenderId(userId);
        return receiverIds.stream()
                .map(receiverId -> {
                    String name = getDisplayName(userId, receiverId);
                    return new PersonDTO(receiverId, name);
                })
                .collect(Collectors.toList());
    }

    // 표시 이름 가져오기
    private String getDisplayName(Long userId, Long targetUserId) {
        return nicknameRepository.findByMemberIdAndAliasMemberId(userId, targetUserId)
                .map(Nickname::getAlias)
                .orElseGet(() -> {
                    return memberRepository.findById(targetUserId)
                            .map(Member::getName)
                            .orElse("알 수 없음");
                });
    }

    // 편지 삭제
    @Transactional
    public void deleteLetter(Long letterId, Long userId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new IllegalArgumentException("편지를 찾을 수 없습니다."));

        if (!letter.getSenderId().equals(userId) && !letter.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        letterRepository.delete(letter);
    }

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
