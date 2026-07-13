package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.BankAccountResponse;
import com.elsys.safebanking.dto.CreateBankAccountRequest;
import com.elsys.safebanking.dto.RecipientAccountResponse;
import com.elsys.safebanking.dto.UpdateBankAccountNameRequest;
import com.elsys.safebanking.exception.AccountNotFoundException;
import com.elsys.safebanking.exception.InvalidRequestException;
import com.elsys.safebanking.model.AccountStatus;
import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.BankAccountRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AccountService {

    private static final int MAX_SAVE_ATTEMPTS = 5;

    private final BankAccountRepository bankAccountRepository;
    private final UserService userService;
    private final AccountIbanGenerator ibanGenerator;
    private final TransactionTemplate transactionTemplate;

    public AccountService(
            BankAccountRepository bankAccountRepository,
            UserService userService,
            AccountIbanGenerator ibanGenerator,
            PlatformTransactionManager transactionManager
    ) {
        this.bankAccountRepository = bankAccountRepository;
        this.userService = userService;
        this.ibanGenerator = ibanGenerator;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public BankAccountResponse createAccount(String email, CreateBankAccountRequest request) {
        for (int attempt = 0; attempt < MAX_SAVE_ATTEMPTS; attempt++) {
            try {
                return transactionTemplate.execute(status -> createAccountInTransaction(email, request));
            } catch (DataIntegrityViolationException exception) {
                if (attempt == MAX_SAVE_ATTEMPTS - 1) {
                    throw exception;
                }
            }
        }
        throw new IllegalStateException("Unable to create account");
    }

    @Transactional(readOnly = true)
    public List<BankAccountResponse> getAccounts(String email) {
        User owner = userService.getByEmail(email);
        return bankAccountRepository.findByOwnerIdOrderByIbanAsc(owner.getId())
                .stream()
                .map(BankAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BankAccountResponse getAccountByIban(String email, String iban) {
        User owner = userService.getByEmail(email);
        return BankAccountResponse.from(getOwnedAccount(owner, iban));
    }

    @Transactional(readOnly = true)
    public RecipientAccountResponse getRecipientAccount(String iban) {
        return bankAccountRepository.findById(iban.trim().toUpperCase())
                .map(RecipientAccountResponse::from)
                .orElseThrow(() -> new AccountNotFoundException(iban));
    }

    @Transactional
    public BankAccountResponse blockAccount(String iban) {
        BankAccount account = bankAccountRepository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException(iban));
        account.block();
        return BankAccountResponse.from(account);
    }

    @Transactional
    public BankAccountResponse unblockAccount(String iban) {
        BankAccount account = bankAccountRepository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException(iban));
        if (account.getStatus() != AccountStatus.BLOCKED) {
            throw new InvalidRequestException("Account " + iban + " is not currently blocked");
        }
        account.unblock();
        return BankAccountResponse.from(account);
    }

    @Transactional
    public BankAccountResponse updateAccountName(String email, String iban, UpdateBankAccountNameRequest request) {
        User owner = userService.getByEmail(email);
        BankAccount account = getOwnedAccount(owner, iban);
        account.updateName(request.name().trim());
        return BankAccountResponse.from(account);
    }

    private BankAccountResponse createAccountInTransaction(String email, CreateBankAccountRequest request) {
        User owner = userService.getByEmail(email);
        BankAccount account = new BankAccount(
                ibanGenerator.generateUniqueIban(),
                request.name().trim(),
                request.currency(),
                owner
        );
        return BankAccountResponse.from(bankAccountRepository.saveAndFlush(account));
    }

    private BankAccount getOwnedAccount(User owner, String iban) {
        return bankAccountRepository.findByIbanAndOwnerId(iban, owner.getId())
                .orElseThrow(() -> new AccountNotFoundException(iban));
    }
}
