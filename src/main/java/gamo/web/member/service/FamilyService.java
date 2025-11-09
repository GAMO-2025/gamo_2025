package gamo.web.member.service;

import gamo.web.member.domain.Family;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.FamilyRepository;
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

//
//    public String joinFamily(Long memberId, String inputCode) {
//        Family family = familyRepository.findByFamilyCode(inputCode)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid family code"));
//
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
//        member.setFamily(family);
//        memberRepository.save(member);
//
//        return inputCode;
//    }
}
