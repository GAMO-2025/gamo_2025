package gamo.web.member.service;

import gamo.web.auth.UserPrincipal;
import gamo.web.auth.refresh.RefreshTokenRepository;
import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.member.domain.DeletedMember;
import gamo.web.member.domain.Member;
import gamo.web.member.domain.Nickname;
import gamo.web.family.dto.FamilyListDTO;
import gamo.web.member.dto.LoginResponseDTO;
import gamo.web.member.repository.DeletedMemberRepository;
import gamo.web.member.repository.MemberRepository;
import gamo.web.member.repository.NicknameRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final DeletedMemberRepository deletedMemberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NicknameRepository nicknameRepository;

    public Long getMemberId(UserPrincipal user) {
        return user.getMember().getId();
    }

    public LoginResponseDTO getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
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

    @Transactional(readOnly = true)
    public List<FamilyListDTO> getFamilyList(Long memberId) {
        Member me = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(me.getFamily() == null) return List.of();

        List<Member> members = memberRepository.findAllByFamilyAndIdNotAndStatus(
                me.getFamily(), memberId, Member.MemberStatus.ACTIVE
        );

        List<Long> aliasIds = members.stream().map(Member::getId).toList();

        Map<Long, String> aliasMap = nicknameRepository
                .findAllByMemberIdAndAliasMemberIdIn(memberId, aliasIds)
                .stream()
                .collect(Collectors.toMap(
                        n -> n.getAliasMember().getId(), // aliasMemberId
                        Nickname::getAlias               // 닉네임 문자열
                ));

        return members.stream()
                .map(m -> new FamilyListDTO(
                        m.getId(),
                        aliasMap.getOrDefault(m.getId(), m.getName()),
                        m.getProfileImage()
                ))
                .toList();
    }
}
