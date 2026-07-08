package com.elsys.safebanking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test — validates Bean Validation constraints on {@link CreateBankAccountRequest}.
 * No Spring context needed; uses the standalone Hibernate Validator.
 */
class CreateBankAccountRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private Set<ConstraintViolation<CreateBankAccountRequest>> validate(CreateBankAccountRequest req) {
        return validator.validate(req);
    }

    // -------------------------------------------------------------------------
    // valid request
    // -------------------------------------------------------------------------

    @Test
    void validRequest_producesNoViolations() {
        CreateBankAccountRequest req = new CreateBankAccountRequest("Savings", "GB29NWBK60161331926819", 1000, "EUR");

        assertThat(validate(req)).isEmpty();
    }

    @Test
    void validRequest_zeroBalance_isAllowed() {
        CreateBankAccountRequest req = new CreateBankAccountRequest("Current", "GB29NWBK60161331926819", 0, "USD");

        assertThat(validate(req)).isEmpty();
    }

    @Test
    void validRequest_negativeBalance_isAllowed() {
        // balance is @NotNull only — negative values are intentionally permitted
        CreateBankAccountRequest req = new CreateBankAccountRequest("Overdrawn", "GB29NWBK60161331926819", -500, "GBP");

        assertThat(validate(req)).isEmpty();
    }

    // -------------------------------------------------------------------------
    // accountName violations
    // -------------------------------------------------------------------------

    @Test
    void nullAccountName_producesViolation() {
        CreateBankAccountRequest req = new CreateBankAccountRequest(null, "GB29NWBK60161331926819", 100, "EUR");

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("accountName"));
    }

    @Test
    void blankAccountName_producesViolation() {
        CreateBankAccountRequest req = new CreateBankAccountRequest("   ", "GB29NWBK60161331926819", 100, "EUR");

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("accountName"));
    }

    @Test
    void emptyAccountName_producesViolation() {
        CreateBankAccountRequest req = new CreateBankAccountRequest("", "GB29NWBK60161331926819", 100, "EUR");

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("accountName"));
    }

    // -------------------------------------------------------------------------
    // iban violations
    // -------------------------------------------------------------------------

    @Test
    void nullIban_producesViolation() {
        CreateBankAccountRequest req = new CreateBankAccountRequest("Savings", null, 100, "EUR");

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("iban"));
    }

    @Test
    void blankIban_producesViolation() {
        CreateBankAccountRequest req = new CreateBankAccountRequest("Savings", "   ", 100, "EUR");

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("iban"));
    }

    // -------------------------------------------------------------------------
    // balance violations
    // -------------------------------------------------------------------------

    @Test
    void nullBalance_producesViolation() {
        CreateBankAccountRequest req = new CreateBankAccountRequest("Savings", "GB29NWBK60161331926819", null, "EUR");

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("balance"));
    }

    // -------------------------------------------------------------------------
    // currency violations
    // -------------------------------------------------------------------------

    @Test
    void nullCurrency_producesViolation() {
        CreateBankAccountRequest req = new CreateBankAccountRequest("Savings", "GB29NWBK60161331926819", 100, null);

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
    }

    @Test
    void blankCurrency_producesViolation() {
        CreateBankAccountRequest req = new CreateBankAccountRequest("Savings", "GB29NWBK60161331926819", 100, "   ");

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
    }

    @Test
    void tooShortCurrency_producesViolation() {
        // "EU" is 2 chars — must be exactly 3
        CreateBankAccountRequest req = new CreateBankAccountRequest("Savings", "GB29NWBK60161331926819", 100, "EU");

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
    }

    @Test
    void tooLongCurrency_producesViolation() {
        // "EURO" is 4 chars — must be exactly 3
        CreateBankAccountRequest req = new CreateBankAccountRequest("Savings", "GB29NWBK60161331926819", 100, "EURO");

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
    }

    @Test
    void exactlyThreeCharCurrency_isValid() {
        CreateBankAccountRequest req = new CreateBankAccountRequest("Savings", "GB29NWBK60161331926819", 100, "BGN");

        assertThat(validate(req)).isEmpty();
    }

    // -------------------------------------------------------------------------
    // multiple violations at once
    // -------------------------------------------------------------------------

    @Test
    void allNullFields_producesViolationForEachField() {
        CreateBankAccountRequest req = new CreateBankAccountRequest(null, null, null, null);

        Set<ConstraintViolation<CreateBankAccountRequest>> violations = validate(req);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("accountName", "iban", "balance", "currency");
    }
}
