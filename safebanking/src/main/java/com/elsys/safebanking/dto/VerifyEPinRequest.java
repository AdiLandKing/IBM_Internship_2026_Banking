package com.elsys.safebanking.dto;

import com.elsys.safebanking.validation.ValidEPin;
import jakarta.validation.constraints.NotBlank;

public record VerifyEPinRequest(
        @NotBlank(message = "E-PIN is required")
        @ValidEPin
        String ePin
) {
}
