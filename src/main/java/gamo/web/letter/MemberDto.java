package gamo.web.letter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String name;

    public static MemberDto from(Member member) {
        return new MemberDto(member.getId(), member.getName());
    }
}
