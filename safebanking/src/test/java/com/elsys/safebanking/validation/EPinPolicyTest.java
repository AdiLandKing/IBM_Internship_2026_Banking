package com.elsys.safebanking.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EPinPolicyTest {

    @Test
    void acceptsExactlySixDigitsIncludingLeadingZeros() {
        assertTrue(EPinPolicy.isValid("000000"));
        assertTrue(EPinPolicy.isValid("012345"));
        assertTrue(EPinPolicy.isValid("999999"));
    }

    @Test
    void rejectsNullBlankNonNumericAndIncorrectLengths() {
        assertFalse(EPinPolicy.isValid(null));
        assertFalse(EPinPolicy.isValid(""));
        assertFalse(EPinPolicy.isValid("      "));
        assertFalse(EPinPolicy.isValid("12345"));
        assertFalse(EPinPolicy.isValid("1234567"));
        assertFalse(EPinPolicy.isValid("12345a"));
        assertFalse(EPinPolicy.isValid("１２３４５６"));
    }

    @Test
    void generatedValuesAlwaysSatisfyPolicy() {
        for (int index = 0; index < 100; index++) {
            assertTrue(EPinPolicy.isValid(EPinPolicy.generate()));
        }
    }

    @Test
    void resolvesManualValueWithoutMarkingItGenerated() {
        EPinPolicy.Resolution resolution = EPinPolicy.resolve("012345");

        assertEquals("012345", resolution.value());
        assertFalse(resolution.generated());
    }

    @Test
    void resolvesEmptyValueToOneTimeGeneratedPin() {
        EPinPolicy.Resolution resolution = EPinPolicy.resolve("");

        assertTrue(EPinPolicy.isValid(resolution.value()));
        assertTrue(resolution.generated());
    }
}
