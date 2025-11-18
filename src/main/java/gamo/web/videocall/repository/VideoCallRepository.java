package gamo.web.videocall.repository;

import gamo.web.member.domain.Member;
import gamo.web.videocall.domain.VideoCall;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoCallRepository extends JpaRepository<VideoCall, Long> {

    @Query("""
    SELECT v FROM VideoCall v
    WHERE (v.caller.id = :userId OR v.receiver.id = :userId)
    ORDER BY v.createdAt DESC, v.id DESC
""")
    List<VideoCall> findByUserIdWithPaging(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT v FROM VideoCall v
    WHERE (v.caller.id = :member1 AND v.receiver.id = :member2) 
    OR (v.caller.id = :member2 AND v.receiver.id = :member1)
    ORDER BY v.createdAt DESC
""")
    List<VideoCall> findCallHistoryByUsersWithPaging(
            @Param("member1") Long member1,
            @Param("member2") Long member2,
            Pageable pageable
    );
}
