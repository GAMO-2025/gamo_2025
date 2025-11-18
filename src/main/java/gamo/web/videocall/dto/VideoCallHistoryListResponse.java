package gamo.web.videocall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoCallHistoryListResponse {
    private List<VideoCallHistoryDTO> content;
    private boolean hasNext;
}
