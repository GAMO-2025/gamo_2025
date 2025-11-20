package gamo.web.videocall.dto;

import gamo.web.videocall.domain.CallType;
import gamo.web.videocall.domain.VideoCall;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoCallResponseDTO {

    public Long videoCallId;
    public Long targetId;
    public String targetNickName;
    public String targetProfileImage;
    public String callType;
    public LocalDateTime createdDate;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public VideoCallResponseDTO(VideoCall videoCall, Long userId) {
        this.videoCallId = videoCall.getId();
        if(userId == videoCall.getCaller().getId()){
            this.targetId = videoCall.getReceiver().getId();
            this.targetNickName = videoCall.getReceiver().getName();
            this.targetProfileImage = videoCall.getReceiver().getProfileImage();
        }
        if(userId == videoCall.getReceiver().getId()){
            this.targetId = videoCall.getCaller().getId();
            this.targetNickName = videoCall.getCaller().getName();
            this.targetProfileImage = videoCall.getCaller().getProfileImage();
        }
        // 포맷팅 추가
        this.createdDate = videoCall.getCreatedAt();
        this.callType = getCallType(videoCall.getCallType());

    }
    public String getCallType(CallType type) {
        return switch (type.toString()) {
            case "COMPLETED" -> "통화종료";
            case "REJECTED" -> "수신거부";
            case "MISSED" -> "부재중";
            case "CANCELLED" -> "발신취소";
            default -> "알수없음";
        };
    }

}
