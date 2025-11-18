package gamo.web.videocall.dto;

import gamo.web.videocall.domain.CallType;
import gamo.web.videocall.domain.VideoCall;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class VideoCallHistoryDTO {

    private Long videoCallId;
    public String callType;
    public LocalDateTime createdDate;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public VideoCallHistoryDTO(VideoCall videoCall) {
        this.videoCallId = videoCall.getId();
        this.createdDate = videoCall.getCreatedAt();
        this.callType = getCallType(videoCall.getCallType());
    }

    public String getCallType(CallType type) {
        return switch (type.toString()) {
            case "COMPLETED" -> "수신";
            case "REJECTED" -> "수신거부";
            case "MISSED" -> "부재중";
            case "CANCELED" -> "발신취소";
            default -> "알수없음";
        };
    }
}
