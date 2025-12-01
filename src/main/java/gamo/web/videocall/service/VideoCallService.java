package gamo.web.videocall.service;

import gamo.web.member.domain.Member;
import gamo.web.member.domain.Nickname;
import gamo.web.member.repository.MemberRepository;
import gamo.web.member.repository.NicknameRepository;

import gamo.web.videocall.domain.CallType;
import gamo.web.videocall.domain.VideoCall;
import gamo.web.videocall.dto.*;
import gamo.web.videocall.repository.VideoCallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCallService {
    private final VideoCallRepository videoCallRepository;
    private final MemberRepository memberRepository;
    private final TopicService topicService;
    private final NicknameRepository nicknameRepository;

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
    public VideoCallListResponse viewVideoCallHistory(Member member, int size) {
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
    public VideoCallHistoryListResponse viewVideoCallHistory(Member member, Long targetId, int size) {
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
        Long latestCallId = videoCallRepository.findLatestCallIdByUserId(userId).get().getId();
        return latestCallId;
    }

    /**
     * 키워드 조회
     * @param member
     * @param targetId
     * @param size
     * @return
     */
    @Transactional(readOnly = true)
    public RecommendDTO.KeywordResponse viewRecommendedKeywords(Member member, Long targetId, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<VideoCall> calls = videoCallRepository.findCallHistoryByUsersWithPaging(member.getId(), targetId, pageable);
        // 최근 call Id 리스트 가져오기
        List<Long> videoCallIds = calls.stream()
                .map(VideoCall::getId)
                .toList();
        // 최근 통화기록이 없을 경우
        if(calls.isEmpty()) {
            log.warn("VideoCallService: 최근 통화기록이 없음 - memberId={}, targetId={}", member.getId(), targetId);
            return new RecommendDTO.KeywordResponse(false, null);
        }
        log.debug("VideoCallService: 조회된 통화 ID 개수={}", videoCallIds.size());
        RecommendDTO.RecommendRequest request = new RecommendDTO.RecommendRequest(videoCallIds);
        // topic service에서 응답 가져오기
        RecommendDTO.RecommendResponse response = topicService.getRecommendedTopic(request);
        RecommendDTO.KeywordResponse keywordResponse = new RecommendDTO.KeywordResponse(true, response.getRecommendedTopic());
        return keywordResponse;
    }

    // 홈화면 - 가장 최근에 통화한 사람과의 키워드 조회
//    @Transactional(readOnly = true)
//    public RecommendDTO.HomeKeywordResponseDTO viewLatestRecommendedKeywords(Member member) {
//        // 가장 최근에 통화한 사람의 userId 조회
//        VideoCall videoCall = videoCallRepository.findLatestCallIdByUserId(member.getId()).get();
//        Long targetId = videoCall.getCaller().getId().equals(member.getId()) ? videoCall.getCaller().getId() : videoCall.getReceiver().getId();
//        // 가장 최근에 통화한 사람 프로필, 이름, 주제 조회
//        RecommendDTO.KeywordResponse keywordResponse = viewRecommendedKeywords(member, targetId, 1);
//        RecommendDTO.HomeKeywordResponseDTO homeKeywordResponse = new RecommendDTO.HomeKeywordResponseDTO(
//                memberRepository.findById(targetId).get().getProfileImage(),
//                nicknameRepository.findByMemberIdAndAliasMemberId(member.getId(),targetId).get().getAlias(),
//                keywordResponse.getTopic()
//        );
//        return homeKeywordResponse;
//    }
    @Transactional(readOnly = true)
    public RecommendDTO.HomeKeywordResponseDTO viewLatestRecommendedKeywords(Member member) {

        // 1. 최근 통화
        Optional<VideoCall> latestCallOpt =
                videoCallRepository.findLatestCallIdByUserId(member.getId());

        if (latestCallOpt.isEmpty()) {
            return null;
        }

        VideoCall latestCall = latestCallOpt.get();

        // 2. 상대방 찾기
        Long targetId = latestCall.getCaller().getId().equals(member.getId()) ?
                latestCall.getReceiver().getId() :
                latestCall.getCaller().getId();

        // 3. 추천 주제 가져오기
        RecommendDTO.KeywordResponse keywordResponse =
                viewRecommendedKeywords(member, targetId, 1);

        String topic = (keywordResponse != null && keywordResponse.isSuccess())
                ? keywordResponse.getTopic()
                : null;

        // 4. 상대방 Member 조회
        Member target = memberRepository.findById(targetId)
                .orElse(null);

        String profileImage = (target != null) ? target.getProfileImage() : null;

        // 5. 닉네임이 없으면 상대방 원래 이름 사용
        String displayName = null;
        if (target != null) {
            Nickname nickname = nicknameRepository
                    .findByMemberIdAndAliasMemberId(member.getId(), targetId)
                    .orElse(null);

            displayName = (nickname != null)
                    ? nickname.getAlias()
                    : target.getName();
        }

        // 6. 결과 리턴
        return new RecommendDTO.HomeKeywordResponseDTO(
                profileImage,
                displayName,
                topic
        );
    }
}
