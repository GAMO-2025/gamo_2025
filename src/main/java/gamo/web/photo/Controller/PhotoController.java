package gamo.web.photo.Controller;

import gamo.web.photo.service.GcpStorageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Binding;
import java.io.IOException;

@Controller
public class PhotoController {
    private final GcpStorageService gcpStorageService;
    // 생성자 주입
    public PhotoController(GcpStorageService gcpStorageService) {
        this.gcpStorageService = gcpStorageService;
    }

    @GetMapping(value = "/photo")
    public String photoForm(Model model) {
        model.addAttribute("photoForm", new PhotoForm());
        return "photoForm";
    }

    @PostMapping(value = "/photo/new")
    public String uploadPhoto(@Valid PhotoForm form, BindingResult result) throws IOException {
//        if (result.hasErrors()) {
//            return "photoForm";
//        }

        //사진 gcp에 업로드
        String newUrl = gcpStorageService.upload(form.getImage());
        //사진 url 받기
        System.out.println(newUrl);

        return "redirect:/";
    }
}
