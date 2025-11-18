package gamo.web.member.repository;

import gamo.web.family.domain.Family;
import gamo.web.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialIdAndProviderAndStatus(String socialId, String provider, Member.MemberStatus status);
    List<Member> findByFamily(Family family);

    @Query("""
    SELECT m
    FROM Member m
    WHERE m.family = (
        SELECT m2.family
        FROM Member m2
        WHERE m2.id = :memberId
    )
    AND m.id <> :memberId
    AND m.status = :status
    """)
    List<Member> findAllByFamilyAndIdNotAndStatus(
            @Param("memberId") Long memberId,
            @Param("status") Member.MemberStatus status
    );
    boolean existsByFamily(Family family);
}
