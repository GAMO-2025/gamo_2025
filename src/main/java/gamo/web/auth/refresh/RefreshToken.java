package gamo.web.auth.refresh;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    @Column(nullable=false)
    private Long memberId;

    @Column(nullable=false, unique=true, length=512)
    private String token;

    @Column(nullable=false)
    private LocalDateTime expiresAt;

}