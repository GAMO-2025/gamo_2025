package gamo.web.member.repository;

import gamo.web.family.domain.Family;
import gamo.web.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialIdAndProviderAndStatus(String socialId, String provider, Member.MemberStatus status);
    List<Member> findByFamily(Family family);

    //나는 제외하고 ACTIVE 상태인 가족들 찾는 함수
    List<Member> findAllByFamilyAndIdNotAndStatus(Family family, Long excludeId, Member.MemberStatus status);
}
