package gamo.web.videocall.service;

import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
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
        return videoCallRepository.findLatestCallIdByUserId(userId)
                .map(VideoCall::getId)
                .orElseThrow(() -> new CustomException(ErrorCode.VIDEO_CALL_NOT_FOUND));
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

    @Transactional
    public void resolveTopicWithRecentCallId(RecommendDTO.TopicProxyRequest request, Long userId) {
        Long callId = getLatestCallId(userId);
        if (callId == null) {
            throw new CustomException(ErrorCode.VIDEO_CALL_NOT_FOUND);
        }
        RecommendDTO.TopicRequest topicRequest = new RecommendDTO.TopicRequest(callId,request.getText());
        topicService.sendTopicRequest(topicRequest);
    }


    // 홈화면 - 가장 최근에 통화한 사람과의 키워드 조회
    @Transactional(readOnly = true)
    public RecommendDTO.HomeKeywordResponseDTO viewLatestRecommendedKeywords(Member member) {

        // 가장 최근 통화 조회 , Optional 에러 처리
        Optional<VideoCall> optionalCall =
                videoCallRepository.findLatestCallIdByUserId(member.getId());

        // 최근 통화기록 없으면 응답 실패로
        if (optionalCall.isEmpty()) {
            log.warn("VideoCallService: 최근 통화기록이 없음 - memberId={}", member.getId());
            return new RecommendDTO.HomeKeywordResponseDTO(false, null, null, null);
        }

        VideoCall videoCall = optionalCall.get();

        // 대상 userId (내가 caller면 receiver, 아니면 caller)
        Long targetId = videoCall.getCaller().getId().equals(member.getId())
                ? videoCall.getReceiver().getId()
                : videoCall.getCaller().getId();

        // 추천 키워드
        RecommendDTO.KeywordResponse keywordResponse =
                viewRecommendedKeywords(member, targetId, 1);

        String topic = null;
        boolean topicSuccess = false;

        if (keywordResponse != null && keywordResponse.isSuccess()) {
            topic = keywordResponse.getTopic();
            topicSuccess = (topic != null);
        }

        // 대상 Member 조회
        Member target = memberRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 닉네임 있으면 닉네임, 없으면 기본 이름!!
        Optional<Nickname> nickOpt =
                nicknameRepository.findByMemberIdAndAliasMemberId(member.getId(), targetId);

        String displayName = nickOpt
                .map(Nickname::getAlias)
                .orElse(target.getName());

        // 최종 응답
        return new RecommendDTO.HomeKeywordResponseDTO(
                topicSuccess,
                target.getProfileImage(),
                displayName,
                topic
        );
    }
}
