package com.elsys.safebanking.dto;

import com.elsys.safebanking.validation.ValidEPin;
import jakarta.validation.constraints.NotBlank;

public record ChangeEPinRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "Current E-PIN is required")
        @ValidEPin
        String currentEPin,

        @NotBlank(message = "New E-PIN is required")
        @ValidEPin
        String newEPin
) {
}
