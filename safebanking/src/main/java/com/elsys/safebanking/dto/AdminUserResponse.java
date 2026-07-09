package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.User;

public record AdminUserResponse(
        String email,
        String firstName,
        String lastName,
        long accountCount,
        boolean active
) {
    public static AdminUserResponse from(User user, long accountCount) {
        return new AdminUserResponse(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                accountCount,
                user.isActive() 
        );
    }
}