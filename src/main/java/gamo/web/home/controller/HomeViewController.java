package gamo.web.home.controller;

import gamo.web.auth.UserPrincipal;
import gamo.web.home.dto.HomeSummaryDTO;
import gamo.web.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class HomeViewController {

    private final HomeService homeService;

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserPrincipal user, Model model) {
        HomeSummaryDTO homeSummary = homeService.getHomeSummary(user.getMember());

        //home.html 에서 ${home.targetNickname} 이런 식으로 접근
        model.addAttribute("home", homeSummary);

        return "pages/home";
    }

}
