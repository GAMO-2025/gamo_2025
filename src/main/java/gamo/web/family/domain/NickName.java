package gamo.web.family.domain;

import gamo.web.member.domain.Member;
import jakarta.persistence.*;

public class NickName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nickname_id")
    private Long nicknameId;

    @Column(length = 20)
    private String nickname;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member userId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member aliasUserId;
}