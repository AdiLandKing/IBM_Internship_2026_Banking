package com.elsys.safebanking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBankAccountRequest(

        @JsonAlias("accountName")
        @NotBlank(message = "Account name is required")
        @Size(max = 80, message = "Account name must be at most 80 characters")
        String name,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "BGN|EUR|USD|GBP", message = "Currency must be one of BGN, EUR, USD, GBP")
        String currency
) {
}
