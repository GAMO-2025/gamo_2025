package gamo.web.videocall.service;

import gamo.web.letter.service.SttService;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import gamo.web.videocall.domain.CallType;
import gamo.web.videocall.domain.VideoCall;
import gamo.web.videocall.dto.VideoCallHistoryDTO;
import gamo.web.videocall.dto.VideoCallHistoryListResponse;
import gamo.web.videocall.dto.VideoCallListResponse;
import gamo.web.videocall.dto.VideoCallResponseDTO;
import gamo.web.videocall.repository.VideoCallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoCallService {
    private final VideoCallRepository videoCallRepository;
    private final MemberRepository memberRepository;

    // 통화 종료 시에 통화 기록 저장
    @Transactional
    public VideoCallResponseDTO saveVideoCall(String fromUserId, String toUserId, CallType callType) {
            Optional<Member> caller = memberRepository.findById(Long.valueOf(fromUserId));
            Optional<Member> receiver = memberRepository.findById(Long.valueOf(toUserId));
            if (caller.isPresent() && receiver.isPresent()) {
                VideoCall newVideoCall = VideoCall.builder()
                        .caller(caller.get())
                        .receiver(receiver.get())
                        .callType(callType)
                        .build();
                videoCallRepository.save(newVideoCall);
                return new VideoCallResponseDTO(newVideoCall, Long.valueOf(fromUserId));
            }
        return null;
    }

    // 통화 기록 최신순 조회
    @Transactional(readOnly = true)
    public VideoCallListResponse viewVideoCallHistory (Member member, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<VideoCall> calls = videoCallRepository.findByUserIdWithPaging(member.getId(), pageable);
        boolean hasNext = calls.size() == size;
        List<VideoCallResponseDTO> content = calls.stream()
                .map(call -> new VideoCallResponseDTO(call, member.getId()))
                .toList();
        return new VideoCallListResponse(content, hasNext);
    }

    // callerId, receiverId로 통화기록을 조회
    @Transactional(readOnly = true)
    public VideoCallHistoryListResponse viewVideoCallHistory (Member member, Long targetId, int size) {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<VideoCall> calls = videoCallRepository.findCallHistoryByUsersWithPaging(member.getId(), targetId, pageable);
        boolean hasNext = calls.size() == size;
        List<VideoCallHistoryDTO> content = calls.stream()
                .map(call -> new VideoCallHistoryDTO(call))
                .toList();
        return new VideoCallHistoryListResponse(content, hasNext);
    }

    // 가장 최근의 통화 기록 조회
    @Transactional(readOnly = true)
    public Long getLatestCallId(Long userId) {
        return videoCallRepository.findLatestCallIdByUserId(userId);
    }

    // 홈 화면 키워드 조회 가장 최근에 통화한 사람과의 추천 키워드 조회

}
