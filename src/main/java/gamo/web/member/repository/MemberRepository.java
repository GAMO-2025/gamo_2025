package gamo.web.member.repository;

import gamo.web.member.domain.Family;
import gamo.web.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialIdAndProvider(String socialId, String provider);
    List<Member> findByFamily(Family family);
}
