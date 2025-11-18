package gamo.web.videocall.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Message {
    private String code;
    private String sender;
    private String receiver;
    private String content;
    private String regdate;
}
