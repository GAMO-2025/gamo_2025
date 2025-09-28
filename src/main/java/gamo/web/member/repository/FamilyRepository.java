package gamo.web.member.repository;

import gamo.web.member.domain.Family;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family, Long> {
}
