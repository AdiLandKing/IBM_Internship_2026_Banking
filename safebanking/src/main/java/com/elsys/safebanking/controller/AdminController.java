package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.service.UserService;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/session")
    public UserProfileResponse session(Principal principal) {
        return UserProfileResponse.from(userService.getByEmail(principal.getName()));
    }
}
