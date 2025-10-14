package gamo.web.letter.repository;

import gamo.web.letter.domain.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface LetterRepository extends JpaRepository<Letter, Long> {

    // 받은 편지 개수
    Long countByReceiverId(Long receiverId);

    // 보낸 편지 개수
    Long countBySenderId(Long senderId);

    // 읽지 않은 편지 개수 (받은 편지 중에서)
    Long countByReceiverIdAndIsReadFalse(Long receiverId);
}
