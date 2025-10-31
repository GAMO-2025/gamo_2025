package gamo.web.member.service;

import gamo.web.auth.UserPrincipal;
import gamo.web.auth.refresh.RefreshTokenRepository;
import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.member.domain.DeletedMember;
import gamo.web.member.domain.Member;
import gamo.web.member.dto.LoginResponseDTO;
import gamo.web.member.repository.DeletedMemberRepository;
import gamo.web.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final DeletedMemberRepository deletedMemberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public Long getMemberId(UserPrincipal user) {
        return user.getMember().getId();
    }

    public LoginResponseDTO getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        return new LoginResponseDTO(member);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(deletedMemberRepository.findById(memberId).isPresent()) {
            return;
        }

        DeletedMember deleted = DeletedMember.builder()
                .member(member)
                .deletedAt(LocalDateTime.now())
                .build();
        deletedMemberRepository.save(deleted);
        refreshTokenRepository.deleteAllByMemberId(memberId);
        member.markDeleted();

    }
}
