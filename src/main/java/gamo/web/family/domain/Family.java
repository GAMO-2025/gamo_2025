package gamo.web.family.domain;

import gamo.web.member.domain.Member;
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

    @Column(name = "family_code", nullable = false, unique = true)
    private String familyCode;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Member> members = new ArrayList<>();
}
