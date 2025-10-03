package gamo.web.family.repository;

import gamo.web.member.domain.Family;
import gamo.web.member.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public Member findById(Long id) {
        return em.find(Member.class, id);
    }
}
