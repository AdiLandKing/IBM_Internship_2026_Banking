package com.elsys.safebanking.controller;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elsys.safebanking.model.AccountStatus;
import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.repository.BankAccountRepository;
import com.elsys.safebanking.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTests {

    private static final String IBAN_PATTERN = "^BG[A-Z0-9]{16}$";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanDatabase() {
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void authenticatedUserCanCreateAccountWithGeneratedIbanAndZeroBalance() throws Exception {
        String token = register("client@example.com");

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Main Account",
                                "currency", "BGN"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iban", matchesPattern(IBAN_PATTERN)))
                .andExpect(jsonPath("$.name").value("Main Account"))
                .andExpect(jsonPath("$.currency").value("BGN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void createdIbansAreUnique() throws Exception {
        String token = register("client@example.com");

        String firstIban = createAccount(token, "Main Account", "BGN");
        String secondIban = createAccount(token, "Reserve Account", "EUR");

        assertNotEquals(firstIban, secondIban);
    }

    @Test
    void authenticatedUserCanRetrieveOnlyTheirAccounts() throws Exception {
        String clientToken = register("client@example.com");
        String otherToken = register("other@example.com");
        String clientIban = createAccount(clientToken, "Main Account", "BGN");
        createAccount(otherToken, "Other Account", "USD");

        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value(clientIban))
                .andExpect(jsonPath("$[0].name").value("Main Account"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void accountsListShowsBlockedStatus() throws Exception {
        String token = register("client@example.com");
        String iban = createAccount(token, "Main Account", "BGN");
        setAccountStatus(iban, AccountStatus.BLOCKED);

        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value(iban))
                .andExpect(jsonPath("$[0].status").value("BLOCKED"));
    }

    @Test
    void authenticatedUserCanRetrieveSingleOwnedAccountByIban() throws Exception {
        String token = register("client@example.com");
        String iban = createAccount(token, "Main Account", "BGN");

        mockMvc.perform(get("/api/accounts/{iban}", iban)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.name").value("Main Account"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void userCannotRetrieveAnotherUsersAccountByIban() throws Exception {
        String clientToken = register("client@example.com");
        String otherToken = register("other@example.com");
        String otherIban = createAccount(otherToken, "Other Account", "USD");

        mockMvc.perform(get("/api/accounts/{iban}", otherIban)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account Not Found"));
    }

    @Test
    void authenticatedUserCanLookupRecipientAccountCurrency() throws Exception {
        String clientToken = register("client@example.com");
        String otherToken = register("other@example.com");
        String otherIban = createAccount(otherToken, "Other Account", "EUR");

        mockMvc.perform(get("/api/accounts/lookup")
                        .param("iban", otherIban)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(otherIban))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").doesNotExist())
                .andExpect(jsonPath("$.name").doesNotExist());
    }

    @Test
    void recipientLookupRejectsUnknownIban() throws Exception {
        String token = register("client@example.com");

        mockMvc.perform(get("/api/accounts/lookup")
                        .param("iban", "BG00UNKNOWNACCOUNT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account Not Found"));
    }

    @Test
    void recipientLookupRejectsBlankIban() throws Exception {
        String token = register("client@example.com");

        mockMvc.perform(get("/api/accounts/lookup")
                        .param("iban", " ")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("IBAN is required."));
    }

    @Test
    void authenticatedUserCanUpdateOwnedAccountName() throws Exception {
        String token = register("client@example.com");
        String iban = createAccount(token, "Main Account", "BGN");

        mockMvc.perform(put("/api/accounts/{iban}", iban)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Operating Account"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.name").value("Operating Account"));
    }

    @Test
void userCanSuspendOwnActiveAccount() throws Exception {
        String token = register("client@example.com");
        String iban = createAccount(token, "Main Account", "BGN");

        mockMvc.perform(put("/api/users/accounts/{iban}/suspend", iban)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void userCanSuspendLegacyAccountWithoutCreatedAt() throws Exception {
        String token = register("client@example.com");
        String iban = createAccount(token, "Main Account", "BGN");
        jdbcTemplate.update("update bank_accounts set created_at = null where iban = ?", iban);

        mockMvc.perform(put("/api/users/accounts/{iban}/suspend", iban)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.status").value("SUSPENDED"))
                .andExpect(jsonPath("$.createdAt").doesNotExist());
    }

    @Test
    void userCannotSuspendAccountThatIsNotActive() throws Exception {
        String token = register("client@example.com");
        String iban = createAccount(token, "Main Account", "BGN");
        setAccountStatus(iban, AccountStatus.SUSPENDED);

        mockMvc.perform(put("/api/users/accounts/{iban}/suspend", iban)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Account State Conflict"));
    }

    @Test
    void userCanActivateOwnSuspendedAccount() throws Exception {
        String token = register("client@example.com");
        String iban = createAccount(token, "Main Account", "BGN");
        setAccountStatus(iban, AccountStatus.SUSPENDED);

        mockMvc.perform(put("/api/users/accounts/{iban}/activate", iban)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void userCannotActivateAccountThatIsAlreadyActive() throws Exception {
        String token = register("client@example.com");
        String iban = createAccount(token, "Main Account", "BGN");

        mockMvc.perform(put("/api/users/accounts/{iban}/activate", iban)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Account State Conflict"));
    }

    @Test
    void userCannotSelfActivateBlockedAccount() throws Exception {
        String token = register("client@example.com");
        String iban = createAccount(token, "Main Account", "BGN");
        setAccountStatus(iban, AccountStatus.BLOCKED);

        mockMvc.perform(put("/api/users/accounts/{iban}/activate", iban)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void userCannotSuspendOrActivateAnotherUsersAccount() throws Exception {
        String clientToken = register("client@example.com");
        String otherToken = register("other@example.com");
        String clientIban = createAccount(clientToken, "Main Account", "BGN");

        mockMvc.perform(put("/api/users/accounts/{iban}/suspend", clientIban)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));

        setAccountStatus(clientIban, AccountStatus.SUSPENDED);

        mockMvc.perform(put("/api/users/accounts/{iban}/activate", clientIban)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void invalidAccountNameAndCurrencyReturnValidationErrors() throws Exception {
        String token = register("client@example.com");

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", " ",
                                "currency", "JPY"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.currency").exists());
    }

    @Test
    void accountEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isForbidden());
    }

    private String register(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", "strongPassword123",
                                "firstName", "Alex",
                                "lastName", "Morgan",
                                "dateOfBirth", "1995-04-12"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not("")))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
    }

    private String createAccount(String token, String name, String currency) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", name,
                                "currency", currency
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iban", matchesPattern(IBAN_PATTERN)))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("iban").asText();
    }

    private void setAccountStatus(String iban, AccountStatus status) {
        BankAccount account = bankAccountRepository.findById(iban).orElseThrow();
        switch (status) {
            case ACTIVE -> account.activate();
            case SUSPENDED -> account.suspend();
            case BLOCKED -> account.block();
        }
        bankAccountRepository.saveAndFlush(account);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
