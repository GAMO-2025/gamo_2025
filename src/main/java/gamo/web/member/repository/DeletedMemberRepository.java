package gamo.web.member.repository;

import gamo.web.member.domain.DeletedMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedMemberRepository extends JpaRepository<DeletedMember, Long> {
}
