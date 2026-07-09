package com.elsys.safebanking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.elsys.safebanking.exception.EPinProtectionException;
import org.junit.jupiter.api.Test;

class EPinCipherTest {

    private static final String TEST_KEY = "test-epin-encryption-key-at-least-32-characters";

    private final EPinCipher cipher = new EPinCipher(TEST_KEY);

    @Test
    void encryptsWithoutPersistingPlaintextAndDecryptsExactly() {
        String ePin = "012345";
        String encrypted = cipher.encrypt(ePin);

        assertNotEquals(ePin, encrypted);
        assertEquals(ePin, cipher.decrypt(encrypted));
    }

    @Test
    void usesUniqueCiphertextForTheSameEPin() {
        assertNotEquals(cipher.encrypt("123456"), cipher.encrypt("123456"));
    }

    @Test
    void rejectsTamperedCiphertextWithDomainSafeException() {
        String encrypted = cipher.encrypt("123456");
        char replacement = encrypted.charAt(encrypted.length() - 1) == 'A' ? 'B' : 'A';
        String tampered = encrypted.substring(0, encrypted.length() - 1) + replacement;

        EPinProtectionException exception = assertThrows(
                EPinProtectionException.class,
                () -> cipher.decrypt(tampered)
        );
        assertEquals("Unable to process E-PIN securely", exception.getMessage());
    }
}
