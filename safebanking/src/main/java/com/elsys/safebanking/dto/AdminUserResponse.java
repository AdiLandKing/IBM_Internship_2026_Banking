package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.User;
import java.util.List;

public record AdminUserResponse(
        String email,
        String firstName,
        String lastName,
        String role,
        long accountCount,
        boolean active,
        List<BankAccountResponse> accounts
) {
    public static AdminUserResponse from(User user, List<BankAccountResponse> accounts) {
        return new AdminUserResponse(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                accounts.size(),
                user.isActive(),
                accounts
        );
    }
}
