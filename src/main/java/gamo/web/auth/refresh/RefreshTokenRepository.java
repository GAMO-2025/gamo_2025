package gamo.web.auth.refresh;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByMemberId(Long memberId); // 나중에 로그아웃 때 쓸 거
}

