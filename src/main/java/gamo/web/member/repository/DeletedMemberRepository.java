package gamo.web.member.repository;

import gamo.web.member.domain.DeletedMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface DeletedMemberRepository extends JpaRepository<DeletedMember, Long> {
    //삭제된 row 수를 반환할거라 long 타입으로
    long deleteByDeletedAtBefore(LocalDateTime threshold);
}
