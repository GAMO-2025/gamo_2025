package gamo.web.family.repository;

import gamo.web.member.domain.Family;
import gamo.web.member.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Getter
@Setter
public class f_FamilyRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(Family family) {
        em.persist(family);
    }

    public Family findByCode(String code) {
        return em.createQuery("SELECT f FROM Family f WHERE f.familyCode = :code", Family.class)
                .setParameter("code", code)
                .getSingleResult();
    }

//    public Family findByCode(String code) {
//        return em.find(Family.class, code);
//    }

    public List<Member> findAll(Long familyId) {
        return em.createQuery(
                        "SELECT m FROM Member m WHERE m.family.id = :familyId", Member.class)
                .setParameter("familyId", familyId)
                .getResultList();
    }
}
