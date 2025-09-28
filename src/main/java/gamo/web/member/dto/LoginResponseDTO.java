package gamo.web.member.dto;

import gamo.web.member.domain.Member;
import lombok.Getter;

@Getter
public class LoginResponseDTO {
    private final Long id;
    private final String name;
    private final String email;
    private final String picture;

    public LoginResponseDTO(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.picture = member.getProfileImage();
    }
}