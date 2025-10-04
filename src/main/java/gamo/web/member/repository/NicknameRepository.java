package gamo.web.member.repository;

import gamo.web.member.domain.Nickname;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NicknameRepository extends JpaRepository<Nickname, Long> {
    Optional<Nickname> findByMemberIdAndAliasMemberId(Long memberId, Long aliasMemberId);
}