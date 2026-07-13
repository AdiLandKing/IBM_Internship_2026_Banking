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
 * Pure unit test — validates Bean Validation constraints on {@link TopUpRequest}.
 * No Spring context; uses standalone Hibernate Validator.
 */
class TopUpRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private Set<ConstraintViolation<TopUpRequest>> validate(TopUpRequest req) {
        return validator.validate(req);
    }

    // -------------------------------------------------------------------------
    // valid requests
    // -------------------------------------------------------------------------

    @Test
    void validRequest_producesNoViolations() {
        assertThat(validate(new TopUpRequest("GB29NWBK60161331926819", 1000L, "eur"))).isEmpty();
    }

    @Test
    void minimumAmount_one_cent_isValid() {
        assertThat(validate(new TopUpRequest("GB29NWBK60161331926819", 1L, "usd"))).isEmpty();
    }

    // -------------------------------------------------------------------------
    // accountIban violations
    // -------------------------------------------------------------------------

    @Test
    void nullAccountIban_producesViolation() {
        Set<ConstraintViolation<TopUpRequest>> violations = validate(new TopUpRequest(null, 1000L, "eur"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("accountIban"));
    }

    @Test
    void blankAccountIban_producesViolation() {
        Set<ConstraintViolation<TopUpRequest>> violations = validate(new TopUpRequest("   ", 1000L, "eur"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("accountIban"));
    }

    // -------------------------------------------------------------------------
    // amountCents violations
    // -------------------------------------------------------------------------

    @Test
    void nullAmountCents_producesViolation() {
        Set<ConstraintViolation<TopUpRequest>> violations = validate(new TopUpRequest("GB29NWBK60161331926819", null, "eur"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amountCents"));
    }

    @Test
    void zeroAmountCents_producesViolation() {
        Set<ConstraintViolation<TopUpRequest>> violations = validate(new TopUpRequest("GB29NWBK60161331926819", 0L, "eur"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amountCents"));
    }

    @Test
    void negativeAmountCents_producesViolation() {
        Set<ConstraintViolation<TopUpRequest>> violations = validate(new TopUpRequest("GB29NWBK60161331926819", -500L, "eur"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amountCents"));
    }

    // -------------------------------------------------------------------------
    // currency violations
    // -------------------------------------------------------------------------

    @Test
    void nullCurrency_producesViolation() {
        Set<ConstraintViolation<TopUpRequest>> violations = validate(new TopUpRequest("GB29NWBK60161331926819", 1000L, null));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
    }

    @Test
    void blankCurrency_producesViolation() {
        Set<ConstraintViolation<TopUpRequest>> violations = validate(new TopUpRequest("GB29NWBK60161331926819", 1000L, "  "));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
    }

    // -------------------------------------------------------------------------
    // multiple violations
    // -------------------------------------------------------------------------

    @Test
    void allNullFields_producesViolationsForEveryField() {
        Set<ConstraintViolation<TopUpRequest>> violations = validate(new TopUpRequest(null, null, null));
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("accountIban", "amountCents", "currency");
    }
}
