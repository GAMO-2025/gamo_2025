package gamo.web.letter.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import gamo.web.letter.domain.InputType;
import gamo.web.letter.domain.Letter;
import gamo.web.letter.dto.LetterRequest;
import gamo.web.letter.repository.LetterRepository;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;
    private final SttService sttService;
    private final GcsService gcsService;

    public Letter sendLetter(Long senderId, LetterRequest request) {
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 발신자 ID"));
        Member receiver = memberRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 수신자 ID"));

        if (!sender.getFamily().getId().equals(receiver.getFamily().getId())) {
            throw new IllegalArgumentException("같은 가족이 아닙니다.");
        }

        // STT 처리
        String content = request.getContent();
        if ("STT".equalsIgnoreCase(request.getInputType()) && request.getVoiceFile() != null) {
            content = sttService.transcribe(request.getVoiceFile());
        }

        // 이미지 저장 - GCS 업로드
        String letterImgPath = null;
        MultipartFile letterImg = request.getLetterImg();
        if (letterImg != null && !letterImg.isEmpty()) {
            letterImgPath = gcsService.uploadFile(letterImg);
        }

        // Letter 엔티티 생성 및 저장
        Letter letter = new Letter();
        letter.setSenderId(senderId);
        letter.setReceiverId(receiver.getId());
        letter.setTitle(request.getTitle());
        letter.setContent(content);
        letter.setLetterImg(letterImgPath);
        letter.setInputType(InputType.valueOf(request.getInputType()));
        letter.setSentAt(LocalDateTime.now());

        return letterRepository.save(letter);
    }
}