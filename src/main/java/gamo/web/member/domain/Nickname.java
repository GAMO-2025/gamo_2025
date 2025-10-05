package gamo.web.member.domain;

import gamo.web.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nickname {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nickname_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_Nickname_Member"))
    // 닉네임 설정한 사람
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alias_member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_Nickname_alias_Member"))
    // 설정 당한 사람
    private Member aliasMember;

    @Column(length = 20)
    private String alias;
}