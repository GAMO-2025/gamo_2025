package gamo.web.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, name = "social_id")
    private String socialId;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(nullable = false, name = "profile_image")
    private String profileImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false,
    foreignKey = @ForeignKey(name = "fk_Member_Family"))
    private Family family;

    public Member update(String name, String picture) {
        this.name = name;
        this.profileImage = picture;
        return this;
    }
}
