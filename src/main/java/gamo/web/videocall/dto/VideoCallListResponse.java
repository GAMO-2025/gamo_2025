package gamo.web.videocall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class VideoCallListResponse {
    private List<VideoCallResponseDTO> content;
    private boolean hasNext;
}
