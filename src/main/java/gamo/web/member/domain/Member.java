package gamo.web.member.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "social_id")
    private String socialId;

    @Column(length = 20)
    private String provider;

    @Column(name = "profile_image")
    private String profileImage;

    public enum MemberStatus {
        ACTIVE,
        DELETED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id",
    foreignKey = @ForeignKey(name = "fk_Member_Family"))
    private Family family;

    public Member update(String name, String picture) {
        this.name = name;
        this.profileImage = picture;
        return this;
    }

    public void markDeleted() {
        this.status = MemberStatus.DELETED;

        // 재가입 시 이메일 충돌 막기 위해 무효 처리 해주기
        this.email = this.email + ".deleted." + System.currentTimeMillis();

        // 카카오 링크 끊기
        this.socialId = null;
        this.provider = null;
        // 이름이랑 프로필 이미지도 null 처리
        this.name = "탈퇴회원";
        this.profileImage = null;
    }
    
    public Member setFamily(Family family) {
        this.family = family;
        return this;
    }
  
}
