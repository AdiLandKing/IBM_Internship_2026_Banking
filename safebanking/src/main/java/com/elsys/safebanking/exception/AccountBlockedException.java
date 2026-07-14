package com.elsys.safebanking.exception;

/**
 * Thrown when a user attempts to activate an account that has been blocked by
 * an administrator. The user-facing activate endpoint (TASK-004) must check
 * for BLOCKED status and throw this exception — only the admin unblock endpoint
 * may clear it.
 */
public class AccountBlockedException extends RuntimeException {

    public AccountBlockedException(String iban) {
        super("Account " + iban + " has been blocked by an administrator and cannot be modified by the user");
    }
}
