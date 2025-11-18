package gamo.web.letter.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;
    private final NicknameRepository nicknameRepository;
    private final GcsService gcsService;

    // 캐시: letterId → signedUrl
    private final Cache<Long, String> signedUrlCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(25))
            .maximumSize(1000)
            .build();

    // -------------------------------
    // 편지 개수 조회
    // -------------------------------
    @Transactional(readOnly = true)
    public LetterCountDTO getLetterCounts(Long userId) {
        Long receivedCount = letterRepository.countByReceiverIdAndIsReceiverDeletedFalseAndIsCancelledFalse(userId);
        Long sentCount = letterRepository.countBySenderIdAndIsSenderDeletedFalseAndIsCancelledFalse(userId);
        Long unreadCount = letterRepository.countByReceiverIdAndIsReceiverDeletedFalseAndIsCancelledFalseAndIsReadFalse(userId);

        return new LetterCountDTO(receivedCount, sentCount, unreadCount);
    }

    // -------------------------------
    // 발신/수신 편지 목록 조회
    // -------------------------------
    @Transactional(readOnly = true)
    public Page<LetterListDTO> getLetters(Long userId, Long otherUserId, boolean received, String sort, int page, int size) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Page<Letter> letterPage;

        if (received) { // 받은 편지
            if (otherUserId != null) {
                letterPage = letterRepository.findByReceiverIdAndSenderIdAndIsCancelledFalseAndIsReceiverDeletedFalse(userId, otherUserId, pageable);
            } else {
                letterPage = letterRepository.findByReceiverIdAndIsCancelledFalseAndIsReceiverDeletedFalse(userId, pageable);
            }
            return letterPage.map(letter -> LetterListDTO.fromReceivedLetter(letter, getDisplayName(userId, letter.getSenderId())));
        } else { // 보낸 편지
            if (otherUserId != null) {
                letterPage = letterRepository.findBySenderIdAndReceiverIdAndIsSenderDeletedFalse(userId, otherUserId, pageable);
            } else {
                letterPage = letterRepository.findBySenderIdAndIsSenderDeletedFalse(userId, pageable);
            }
            return letterPage.map(letter -> LetterListDTO.fromSentLetter(letter, getDisplayName(userId, letter.getReceiverId())));
        }
    }

    // -------------------------------
    // 발신자/수신자 목록 조회
    // -------------------------------
    @Transactional(readOnly = true)
    public List<PersonDTO> getLetterPeople(Long userId, boolean received) {
        if (received) {
            List<Long> senderIds = letterRepository.findDistinctSenderIdsByReceiverIdAndIsCancelledFalse(userId);
            return senderIds.stream()
                    .map(senderId -> new PersonDTO(senderId, getDisplayName(userId, senderId)))
                    .collect(Collectors.toList());
        } else {
            List<Long> receiverIds = letterRepository.findDistinctReceiverIdsBySenderId(userId);
            return receiverIds.stream()
                    .map(receiverId -> new PersonDTO(receiverId, getDisplayName(userId, receiverId)))
                    .collect(Collectors.toList());
        }
    }

    // -------------------------------
    // 편지 상세 조회
    // -------------------------------
    @Transactional(readOnly = true)
    public LetterDetailDTO getLetterDetail(Long letterId, Long currentUserId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new CustomException(ErrorCode.LETTER_NOT_FOUND));

        if (!letter.getSenderId().equals(currentUserId) && !letter.getReceiverId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.LETTER_ACCESS_FORBIDDEN);
        }

        if (letter.getReceiverId().equals(currentUserId) && !letter.isRead()) {
            letter.setRead(true);
            letter.setReadAt(LocalDateTime.now());
            letterRepository.save(letter);
        }

        String signedUrl = null;
        if (letter.getLetterImg() != null && letter.getLetterImg().startsWith("gs://")) {
            signedUrl = signedUrlCache.get(letterId, k -> {
                try {
                    return gcsService.generateSignedUrl(letter.getLetterImg(), 30).toString();
                } catch (Exception e) {
                    log.error("Letter ID {}의 Signed URL 생성 실패: {}", letterId, letter.getLetterImg(), e);
                    throw new CustomException(ErrorCode.LETTER_IMAGE_SIGNED_URL_FAILED);
                }
            });
        }

        String senderName = getDisplayName(currentUserId, letter.getSenderId());
        String receiverName = getDisplayName(currentUserId, letter.getReceiverId());

        return LetterDetailDTO.fromEntityWithSignedUrl(letter, senderName, receiverName, currentUserId, signedUrl);
    }

    // -------------------------------
    // 편지 전송
    // -------------------------------
    @Transactional
    public Letter sendLetter(Long senderId, LetterRequestDTO request) {
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Member receiver = memberRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!sender.getFamily().getId().equals(receiver.getFamily().getId())) {
            throw new CustomException(ErrorCode.NOT_SAME_FAMILY);
        }

        String letterImgPath = null;
        if (request.getLetterImg() != null && !request.getLetterImg().isEmpty()) {
            try {
                letterImgPath = gcsService.uploadFile(request.getLetterImg());
                log.info("Letter {}: 이미지 업로드 성공 {}", senderId, letterImgPath);
            } catch (Exception e) {
                log.error("Letter {}: 이미지 업로드 실패", senderId, e);
                throw new CustomException(ErrorCode.LETTER_IMAGE_UPLOAD_FAILED);
            }
        }

        Letter letter = Letter.builder()
                .senderId(senderId)
                .receiverId(receiver.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .letterImg(letterImgPath)
                .inputType(InputType.valueOf(request.getInputType()))
                .isSenderDeleted(false)
                .isReceiverDeleted(false)
                .build();

        return letterRepository.save(letter);
    }

    // -------------------------------
    // 편지 삭제
    // -------------------------------
    @Transactional
    public void deleteLetter(Long letterId, Long userId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new CustomException(ErrorCode.LETTER_NOT_FOUND));

        boolean isSender = letter.getSenderId().equals(userId);
        boolean isReceiver = letter.getReceiverId().equals(userId);

        if (!isSender && !isReceiver) throw new CustomException(ErrorCode.LETTER_DELETE_FORBIDDEN);

        // 전송취소 된 편지는 sender가 삭제 시 바로 완전 삭제
        if (letter.isCancelled() && isSender) {
            if (letter.getLetterImg() != null && letter.getLetterImg().startsWith("gs://")) {
                try {
                    gcsService.deleteFile(letter.getLetterImg());
                } catch (Exception e) {
                    log.warn("GCS 이미지 삭제 실패: {}", letter.getLetterImg(), e);
                }
            }
            letterRepository.delete(letter);
            return;
        }

        // 편지 일반 삭제 처리 (soft delete)
        if (isSender) {
            if (letter.getIsSenderDeleted()) throw new CustomException(ErrorCode.LETTER_ALREADY_DELETED);
            letter.setIsSenderDeleted(true);
        }
        if (isReceiver) {
            if (letter.getIsReceiverDeleted()) throw new CustomException(ErrorCode.LETTER_ALREADY_DELETED);
            letter.setIsReceiverDeleted(true);
        }

        // 발신자/수신자 모두 삭제 시 hard delete
        if (letter.getIsSenderDeleted() && letter.getIsReceiverDeleted()) {
            if (letter.getLetterImg() != null && letter.getLetterImg().startsWith("gs://")) {
                try {
                    gcsService.deleteFile(letter.getLetterImg());
                } catch (Exception e) {
                    log.warn("GCS 이미지 삭제 실패: {}", letter.getLetterImg(), e);
                }
            }
            letterRepository.delete(letter);
        } else {
            letterRepository.save(letter);
        }
    }

    // -------------------------------
    // 편지 취소
    // -------------------------------
    @Transactional
    public void cancelLetter(Long letterId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new CustomException(ErrorCode.LETTER_NOT_FOUND));
        letter.setCancelled(true);
    }

    // -------------------------------
    // 표시 이름 조회 (별명 우선)
    // -------------------------------
    @Transactional(readOnly = true)
    public String getDisplayName(Long userId, Long targetUserId) {
        return nicknameRepository.findByMemberIdAndAliasMemberId(userId, targetUserId)
                .map(Nickname::getAlias)
                .orElseGet(() -> memberRepository.findById(targetUserId)
                        .map(Member::getName)
                        .orElse("알 수 없음"));
    }
}
