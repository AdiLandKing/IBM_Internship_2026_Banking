package com.elsys.safebanking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping({"/accounts", "/admin", "/portfolio", "/profile", "/transactions"})
    public String frontendRoute() {
        return "forward:/index.html";
    }
}
