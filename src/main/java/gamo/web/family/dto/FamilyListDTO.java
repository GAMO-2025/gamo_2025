package gamo.web.family.dto;

public record FamilyListDTO (
        Long id,          // 그 가족의 memberId
        String nickname,
        String profileImage
) {}
