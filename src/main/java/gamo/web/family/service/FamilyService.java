package gamo.web.family.service;

import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.family.domain.Family;
import gamo.web.family.repository.FamilyRepository;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FamilyService {
    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    public boolean hasFamily(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return member.getFamily() != null;
    }

    public String getFamilyCode(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Family family = member.getFamily();

        if(family == null) {
            throw new CustomException(ErrorCode.FAMILY_NOT_FOUND);
        }

        return family.getFamilyCode();
    }

    // 초대코드화면...
    @Transactional
    public void inviteFamily(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        //이미 가족에 초대되었다면 에러 보내기
        if(member.getFamily() != null) {
            throw new CustomException(ErrorCode.FAMILY_EXISTS);
        }

        int retryCount = 0;
        Family family = null;

        //만약 랜덤하게 생성된 코드가 unique 하지 않다면 최대 3번까지 돌면서 다시 생성
        while(retryCount < 3) {
            try {
                String code = UUID.randomUUID().toString().substring(0, 13);

                family = Family.builder()
                        .familyCode(code)
                        .build();

                familyRepository.save(family);
                break;
            } catch (DataIntegrityViolationException e) {
                retryCount++;
                if(retryCount >= 3) {
                    throw new CustomException(ErrorCode.FAMILY_CODE_DUPLICATED);
                }
            }
        }

        // member 와 family 연결
        member.setFamily(family);
        memberRepository.save(member);
    }

    // 초대코드 입력하면 그 가족에 가입
    @Transactional
    public void joinFamily(Long memberId, String inviteCode) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Family family = familyRepository.findByFamilyCode(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.FAMILY_NOT_FOUND));

        member.setFamily(family);
        memberRepository.save(member);
    }

    // 가족 탈퇴
    @Transactional
    public void leaveFamily(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Family family = member.getFamily();
        if(family == null) {
            throw new CustomException(ErrorCode.FAMILY_NOT_FOUND);
        }

        member.setFamily(null);

        // 만약, 가족 구성원이 한 명도 안남아있다면 그 가족 엔티티를 삭제한다
        if(!memberRepository.existsByFamily(family)) {
            familyRepository.delete(family);
        }
    }
}
