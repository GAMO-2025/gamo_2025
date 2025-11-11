package gamo.web.family.service;

import gamo.web.family.domain.Family;
import gamo.web.member.domain.Member;
import gamo.web.family.repository.FamilyRepository;
import gamo.web.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyService {
    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    public void createFamily(Long memberId) {
        String familyCode = UUID.randomUUID().toString();
        Family family = new Family(familyCode);
        familyRepository.save(family);

        Member member = memberRepository.findById(memberId).get();
        member.setFamily(family);
        memberRepository.save(member);
    }

    public Boolean isFamilyEmpty(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if(member.getFamily() != null) return false;
        else return true;
    }

    public String getFamilyCode(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if(member.getFamily() == null)
            throw new IllegalStateException("사용자가 소속된 가족 그룹이 없습니다.");
        return member.getFamily().getFamilyCode();
    }

    public boolean joinFamily(String inviteCode, Long memberId) {
        Family family = familyRepository.findByFamilyCode(inviteCode)
                .orElse(null);
        if(family == null) {
            return false;
        }

        Member member = memberRepository.findById(memberId) //member가 존재하는지 확인
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if(member.getFamily() != null) { //이미 가족그룹에 가입한 경우
            return false;
        }
        member.setFamily(family);
        memberRepository.save(member);

        return true;
    }
}
