package gamo.web.member.service;

import gamo.web.auth.UserPrincipal;
import gamo.web.member.domain.Member;
import gamo.web.member.dto.LoginResponseDTO;
import gamo.web.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;

    public Long getMemberId(UserPrincipal user) {
        return user.getMember().getId();
    }

    public LoginResponseDTO getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        return new LoginResponseDTO(member);
    }
}
