package gamo.web.family.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class FamilyController {
    @GetMapping("/")
    public String test() {
        return "test";
    }
}
