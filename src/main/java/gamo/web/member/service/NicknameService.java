package gamo.web.member.service;

import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.member.domain.Member;
import gamo.web.member.domain.Nickname;
import gamo.web.member.repository.MemberRepository;
import gamo.web.member.repository.NicknameRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class NicknameService {
    private final NicknameRepository nicknameRepository;
    private final MemberRepository memberRepository;

    public void upsertNickname(Long memberId, Long aliasMemberId, String nickname) {
        Member member = memberRepository.getReferenceById(memberId);
        Member aliasMember = memberRepository.getReferenceById(aliasMemberId);

        if(!member.getFamily().equals(aliasMember.getFamily())) {
            throw new CustomException(ErrorCode.NOT_SAME_FAMILY);
        }

        nicknameRepository.findByMemberIdAndAliasMemberId(memberId, aliasMemberId)
                .ifPresentOrElse(
                        n -> n.setAlias(nickname),
                        () -> nicknameRepository.save(
                                Nickname.builder()
                                        .member(member)
                                        .aliasMember(aliasMember)
                                        .alias(nickname)
                                        .build()
                        )
                );
    }
}
