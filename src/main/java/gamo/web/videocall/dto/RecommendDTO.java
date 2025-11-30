package gamo.web.videocall.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class RecommendDTO {

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class RecommendRequest {

        @JsonProperty("videocall_ids")
        private List<Long> videoCallIds;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    public static class RecommendResponse {
        private Integer status;
        @JsonProperty("recommended_topic")
        private String recommendedTopic;
    }

    @AllArgsConstructor
    @Getter
    public static class KeywordResponse {
        private boolean success;
        private String topic;
    }

    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class HomeKeywordResponseDTO {
        private String profileImage;
        private String name;
        private String topic;
    }

}
