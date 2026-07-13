package com.elsys.safebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBankAccountNameRequest(
        @NotBlank(message = "Account name is required")
        @Size(max = 80, message = "Account name must be at most 80 characters")
        String name
) {
}
