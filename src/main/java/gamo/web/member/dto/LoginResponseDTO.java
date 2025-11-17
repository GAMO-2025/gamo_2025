package gamo.web.member.dto;

import gamo.web.member.domain.Member;
import lombok.Getter;

@Getter
public class LoginResponseDTO {
    private final String name;
    private final String email;
    private final String picture;

    public LoginResponseDTO(Member member) {
        this.name = member.getName();
        this.email = member.getEmail();
        this.picture = member.getProfileImage();
    }
}