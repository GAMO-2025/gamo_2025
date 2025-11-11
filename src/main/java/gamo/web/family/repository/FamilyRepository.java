package gamo.web.family.repository;

import gamo.web.family.domain.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByFamilyCode(String familyCode);
}
