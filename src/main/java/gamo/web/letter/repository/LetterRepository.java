package gamo.web.letter.repository;

import gamo.web.letter.domain.Letter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, Long> {

    // 받은 편지 목록 조회 (페이징)
    Page<Letter> findByReceiverId(Long receiverId, Pageable pageable);

    // 받은 편지 목록 조회 - 발신자 필터 (페이징)
    Page<Letter> findByReceiverIdAndSenderId(Long receiverId, Long senderId, Pageable pageable);

    // 보낸 편지 목록 조회 (페이징)
    Page<Letter> findBySenderId(Long senderId, Pageable pageable);

    // 보낸 편지 목록 조회 - 수신자 필터 (페이징)
    Page<Letter> findBySenderIdAndReceiverId(Long senderId, Long receiverId, Pageable pageable);

    // 받은 편지 개수
    Long countByReceiverId(Long receiverId);

    // 보낸 편지 개수
    Long countBySenderId(Long senderId);

    // 읽지 않은 편지 개수 (받은 편지 중에서)
    Long countByReceiverIdAndIsReadFalse(Long receiverId);

    // 받은 편지의 발신자 ID 목록 (중복 제거)
    @Query("SELECT DISTINCT l.senderId FROM Letter l WHERE l.receiverId = :receiverId")
    List<Long> findDistinctSenderIdsByReceiverId(@Param("receiverId") Long receiverId);

    // 보낸 편지의 수신자 ID 목록 (중복 제거)
    @Query("SELECT DISTINCT l.receiverId FROM Letter l WHERE l.senderId = :senderId")
    List<Long> findDistinctReceiverIdsBySenderId(@Param("senderId") Long senderId);
}
