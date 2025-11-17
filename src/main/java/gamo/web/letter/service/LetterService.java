package gamo.web.letter.service;

import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.letter.domain.InputType;
import gamo.web.letter.domain.Letter;
import gamo.web.letter.dto.*;
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

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;
    private final NicknameRepository nicknameRepository;
    private final GcsService gcsService;

    // 편지 개수 조회
    public LetterCountDTO getLetterCounts(Long userId) {
        Long receivedCount = letterRepository.countByReceiverIdAndIsReceiverDeletedFalseAndIsCancelledFalse(userId);
        Long sentCount = letterRepository.countBySenderIdAndIsSenderDeletedFalseAndIsCancelledFalse(userId);
        Long unreadCount = letterRepository.countByReceiverIdAndIsReceiverDeletedFalseAndIsCancelledFalseAndIsReadFalse(userId);

        return new LetterCountDTO(receivedCount, sentCount, unreadCount);
    }

    // 받은 편지 목록 조회
    public Page<LetterListDTO> getReceivedLetters(Long userId, Long senderId, String sort, int page, int size) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Page<Letter> letterPage;
        if (senderId != null) {
            letterPage = letterRepository.findByReceiverIdAndSenderIdAndIsCancelledFalseAndIsReceiverDeletedFalse(userId, senderId, pageable);
        } else {
            letterPage = letterRepository.findByReceiverIdAndIsCancelledFalseAndIsReceiverDeletedFalse(userId, pageable);
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
            letterPage = letterRepository.findBySenderIdAndReceiverIdAndIsSenderDeletedFalse(userId, receiverId, pageable);
        } else {
            letterPage = letterRepository.findBySenderIdAndIsSenderDeletedFalse(userId, pageable);
        }

        return letterPage.map(letter -> {
            String receiverName = getDisplayName(userId, letter.getReceiverId());
            return LetterListDTO.fromSentLetter(letter, receiverName);
        });
    }

    // 받은 편지의 발신자 목록
    public List<PersonDTO> getReceivedLetterSenders(Long userId) {
        List<Long> senderIds = letterRepository.findDistinctSenderIdsByReceiverIdAndIsCancelledFalse(userId);
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
                .orElseThrow(() -> new CustomException(ErrorCode.LETTER_NOT_FOUND));

        boolean isSender = letter.getSenderId().equals(userId);
        boolean isReceiver = letter.getReceiverId().equals(userId);

        if (!isSender && !isReceiver) {
            throw new CustomException(ErrorCode.LETTER_DELETE_FORBIDDEN);
        }

        // 보낸 사람 삭제
        if (isSender) {
            if (letter.getIsSenderDeleted()) {
                throw new CustomException(ErrorCode.LETTER_ALREADY_DELETED);
            }
            letter.setIsSenderDeleted(true);
        }

        // 받은 사람 삭제
        if (isReceiver) {
            if (letter.getIsReceiverDeleted()) {
                throw new CustomException(ErrorCode.LETTER_ALREADY_DELETED);
            }
            letter.setIsReceiverDeleted(true);
        }

        // 둘 다 삭제한 경우 DB에서 완전 삭제 (Hard Delete)
        if (letter.getIsSenderDeleted() && letter.getIsReceiverDeleted()) {
            letterRepository.delete(letter);
        } else {
            // 둘 중 한 명만 삭제했을 경우 soft delete 유지
            letterRepository.save(letter);
        }
    }

    // 편지 상세 조회
    @Transactional
    public LetterDetailDTO getLetterDetail(Long letterId, Long currentUserId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new CustomException(ErrorCode.LETTER_NOT_FOUND));

        // 권한 확인 (발신자 또는 수신자만 조회 가능)
        if (!letter.getSenderId().equals(currentUserId) && !letter.getReceiverId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.LETTER_ACCESS_FORBIDDEN);
        }

        // 수신자가 조회하고 아직 읽지 않았다면 읽음 처리
        if (letter.getReceiverId().equals(currentUserId) && !letter.isRead()) {
            letter.setRead(true);
            letter.setReadAt(LocalDateTime.now());
            letterRepository.save(letter);
        }

        // signedUrl 생성
        String signedUrl = null;
        if (letter.getLetterImg() != null && letter.getLetterImg().startsWith("gs://")) {
            try {
                signedUrl = gcsService.generateSignedUrl(letter.getLetterImg(), 30).toString();
            } catch (Exception e) {
                throw new CustomException(ErrorCode.FILE_SIGNED_URL_ERROR);
            }
        }

        // 발신자와 수신자 이름 가져오기 (별명 우선)
        String senderName = getDisplayName(currentUserId, letter.getSenderId());
        String receiverName = getDisplayName(currentUserId, letter.getReceiverId());

        return LetterDetailDTO.fromEntityWithSignedUrl(letter, senderName, receiverName, currentUserId, signedUrl);
    }

    // 편지 작성 화면용 가족 목록
    public List<FamilyDisplay> getFamilyDisplayList(Long loginMemberId) {
        Member me = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return nicknameRepository.findByMemberIdAndAliasMemberId(senderId, receiver.getId())
                .map(Nickname::getAlias)
                .orElse(receiver.getName());
    }

    // 편지 전송
    public Letter sendLetter(Long senderId, LetterRequestDTO request) {
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Member receiver = memberRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!sender.getFamily().getId().equals(receiver.getFamily().getId())) {
            throw new CustomException(ErrorCode.NOT_SAME_FAMILY);
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
                .isSenderDeleted(false)
                .isReceiverDeleted(false)
                .build();

        return letterRepository.save(letter);
    }

    // 편지 취소
    @Transactional
    public void cancelLetter(Long letterId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new CustomException(ErrorCode.LETTER_NOT_FOUND));
        letter.setCancelled(true);
    }

    public record FamilyDisplay(Long id, String displayName) {}

}
