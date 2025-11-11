package gamo.web.family.repository;

import gamo.web.family.domain.Family;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family, Long> {
}
