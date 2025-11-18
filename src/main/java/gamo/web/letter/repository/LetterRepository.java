package gamo.web.letter.repository;

import gamo.web.letter.domain.Letter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, Long> {

    // [보낸 편지함] 삭제하지 않은 모든 보낸 편지 (삭제 미포함, 취소 포함)
    Page<Letter> findBySenderIdAndIsSenderDeletedFalse(Long senderId, Pageable pageable);

    // [보낸 편지함] 특정 수신자에게 보낸 편지 (삭제 미포함, 취소 포함)
    Page<Letter> findBySenderIdAndReceiverIdAndIsSenderDeletedFalse(Long senderId, Long receiverId, Pageable pageable);

    // [받은 편지함] 삭제하지 않았고, 취소되지 않은 편지 (삭제 미포함, 취소 미포함)
    Page<Letter> findByReceiverIdAndIsCancelledFalseAndIsReceiverDeletedFalse(Long receiverId, Pageable pageable);

    // [받은 편지함] 특정 발신자로부터 받은 편지 (삭제 미포함, 취소 미포함)
    Page<Letter> findByReceiverIdAndSenderIdAndIsCancelledFalseAndIsReceiverDeletedFalse(Long receiverId, Long senderId, Pageable pageable);

    // 내가 수신자일 때 삭제하지 않은 받은 편지 개수 (취소되지 않은 것)
    Long countByReceiverIdAndIsReceiverDeletedFalseAndIsCancelledFalse(Long receiverId);

    // 읽지 않은 받은 편지 개수
    Long countByReceiverIdAndIsReceiverDeletedFalseAndIsCancelledFalseAndIsReadFalse(Long receiverId);

    // 내가 발신자일 때 삭제하지 않은 보낸 편지 개수 (취소되지 않은 것)
    Long countBySenderIdAndIsSenderDeletedFalseAndIsCancelledFalse(Long senderId);

    // 전송 취소된 편지 수 (발신자 기준)
    Long countBySenderIdAndIsCancelledTrue(Long senderId);

    // 취소되지 않은 받은 편지 발신자 ID 목록
    @Query("SELECT DISTINCT l.senderId FROM Letter l WHERE l.receiverId = :receiverId AND l.isCancelled = false")
    List<Long> findDistinctSenderIdsByReceiverIdAndIsCancelledFalse(@Param("receiverId") Long receiverId);

    // 보낸 편지 수신자 ID 전체 목록
    @Query("SELECT DISTINCT l.receiverId FROM Letter l WHERE l.senderId = :senderId")
    List<Long> findDistinctReceiverIdsBySenderId(@Param("senderId") Long senderId);
}
