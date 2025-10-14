package gamo.web.letter.domain;

import gamo.web.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "letter")
public class Letter extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "letter_img")
    private String letterImg;   // 파일 경로

    @Enumerated(EnumType.STRING)
    private InputType inputType; // TEXT, STT

    @Column(name = "is_cancelled")
    private boolean isCancelled = false;

    @Column(name = "is_read")
    private boolean isRead = false;
}

