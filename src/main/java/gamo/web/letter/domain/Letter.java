package gamo.web.letter.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "letter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Letter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String letterImg;   // 파일 경로

    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private InputType inputType; // TEXT, STT

    private boolean cancelled = false;
}

