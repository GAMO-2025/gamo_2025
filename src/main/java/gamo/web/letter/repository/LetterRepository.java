package gamo.web.letter.repository;

import gamo.web.letter.domain.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface LetterRepository extends JpaRepository<Letter, Long> {
}
