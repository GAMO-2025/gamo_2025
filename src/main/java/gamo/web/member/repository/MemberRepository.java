package gamo.web.member.repository;

import gamo.web.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialIdAndProvider(String socialId, String provider);
    Optional<Member> findById(Long id);
}
