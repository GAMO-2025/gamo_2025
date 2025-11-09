package gamo.web.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_id")
    private Long id;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Member> members = new ArrayList<>();

    @Column(nullable = false, unique = true)
    private String familyCode;

    public Family(String familyCode) { //familyCode 필수
        if (familyCode == null || familyCode.isBlank()) {
            throw new IllegalArgumentException("Family code가 입력되지 않았습니다.");
        }
        this.familyCode = familyCode;
    }

    public void addMember(Member member) { //양방향
        members.add(member);
        member.setFamily(this);
    }
}
