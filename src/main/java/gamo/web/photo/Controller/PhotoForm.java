package gamo.web.photo.Controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class PhotoForm {
    @NotNull(message = "사진이 선택되지 않았습니다")
    private MultipartFile image;
}
