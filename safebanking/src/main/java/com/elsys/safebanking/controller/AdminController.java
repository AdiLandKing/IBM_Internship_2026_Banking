package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.AdminTransactionResponse;
import com.elsys.safebanking.dto.AdminUserResponse;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.service.AdminTransactionService;
import com.elsys.safebanking.service.UserService;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final AdminTransactionService adminTransactionService;

    public AdminController(UserService userService, AdminTransactionService adminTransactionService) {
        this.userService = userService;
        this.adminTransactionService = adminTransactionService;
    }

    @GetMapping("/session")
    public UserProfileResponse session(Principal principal) {
        return UserProfileResponse.from(userService.getByEmail(principal.getName()));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminUserResponse> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminTransactionResponse> getAllTransactions(@PageableDefault(size = 20) Pageable pageable) {
        return adminTransactionService.getAllTransactions(pageable);
    }
}