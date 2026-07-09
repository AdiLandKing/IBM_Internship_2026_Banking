package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.AdminUserResponse;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.service.AdminService;
import com.elsys.safebanking.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    public AdminController(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @GetMapping("/session")
    public UserProfileResponse session(Principal principal) {
        return UserProfileResponse.from(userService.getByEmail(principal.getName()));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") 
    public Page<AdminUserResponse> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return adminService.getAllUsers(pageable);
    }
}