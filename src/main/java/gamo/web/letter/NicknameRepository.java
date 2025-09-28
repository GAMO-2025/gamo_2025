package gamo.web.letter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NicknameRepository extends JpaRepository<Nickname, Long> {
    Optional<Nickname> findByUserIdAndAliasUserId(Long userId, Long aliasUserId);
}

