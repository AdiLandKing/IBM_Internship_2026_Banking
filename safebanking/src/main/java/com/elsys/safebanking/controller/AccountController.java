package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.BankAccountResponse;
import com.elsys.safebanking.dto.CreateBankAccountRequest;
import com.elsys.safebanking.dto.RecipientAccountResponse;
import com.elsys.safebanking.dto.UpdateBankAccountNameRequest;
import com.elsys.safebanking.service.AccountService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccountResponse createAccount(
            Principal principal,
            @Valid @RequestBody CreateBankAccountRequest request
    ) {
        return accountService.createAccount(principal.getName(), request);
    }

    @GetMapping
    public List<BankAccountResponse> accounts(Principal principal) {
        return accountService.getAccounts(principal.getName());
    }

    @GetMapping("/lookup")
    public RecipientAccountResponse recipientAccount(@RequestParam String iban) {
        return accountService.getRecipientAccount(iban);
    }

    @GetMapping("/{iban}")
    public BankAccountResponse account(
            Principal principal,
            @PathVariable String iban
    ) {
        return accountService.getAccountByIban(principal.getName(), iban);
    }

    @PutMapping("/{iban}")
    public BankAccountResponse updateAccountName(
            Principal principal,
            @PathVariable String iban,
            @Valid @RequestBody UpdateBankAccountNameRequest request
    ) {
        return accountService.updateAccountName(principal.getName(), iban, request);
    }
}
