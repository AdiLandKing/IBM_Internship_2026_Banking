package com.elsys.safebanking.service;

import com.elsys.safebanking.repository.BankAccountRepository;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class AccountIbanGenerator {

    public static final int IBAN_LENGTH = 18;
    public static final String IBAN_PREFIX = "BG";
    private static final int RANDOM_SUFFIX_LENGTH = 16;
    private static final int MAX_GENERATION_ATTEMPTS = 100;
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();
    private final BankAccountRepository bankAccountRepository;

    public AccountIbanGenerator(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public String generateUniqueIban() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = generateCandidate();
            if (!bankAccountRepository.existsByIban(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique account IBAN");
    }

    private String generateCandidate() {
        StringBuilder iban = new StringBuilder(IBAN_LENGTH);
        iban.append(IBAN_PREFIX);
        for (int index = 0; index < RANDOM_SUFFIX_LENGTH; index++) {
            iban.append(ALPHABET[secureRandom.nextInt(ALPHABET.length)]);
        }
        return iban.toString();
    }
}
