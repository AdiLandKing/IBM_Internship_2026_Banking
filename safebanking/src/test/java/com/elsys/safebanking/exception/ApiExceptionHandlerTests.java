package com.elsys.safebanking.exception;

import com.elsys.safebanking.service.AppUserDetailsService;
import com.elsys.safebanking.service.AuthService;
import com.elsys.safebanking.service.JwtService;
import com.elsys.safebanking.service.TransferService;
import com.elsys.safebanking.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandlerTests.StubController.class, ApiExceptionHandler.class})
class ApiExceptionHandlerTests {

    @RestController
    static class StubController {
        @GetMapping("/stub/account-not-found")
        void accountNotFound() {
            throw new AccountNotFoundException("Account not found");
        }

        @GetMapping("/stub/insufficient-funds")
        void insufficientFunds() {
            throw new InsufficientFundsException("Insufficient funds");
        }

        @GetMapping("/stub/account-suspended")
        void accountSuspended() {
            throw new AccountSuspendedException("Account is suspended");
        }

        @GetMapping("/stub/account-ownership")
        void accountOwnership() {
            throw new AccountOwnershipException("Account does not belong to you");
        }

        @GetMapping("/stub/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("Access denied");
        }
    }

    @MockitoBean
    AppUserDetailsService appUserDetailsService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    TransferService transferService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void accountNotFoundException_returns404() throws Exception {
        mockMvc.perform(get("/stub/account-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void insufficientFundsException_returns422() throws Exception {
        mockMvc.perform(get("/stub/insufficient-funds"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    void accountSuspendedException_returns409() throws Exception {
        mockMvc.perform(get("/stub/account-suspended"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Account is suspended"));
    }

    @Test
    void accountOwnershipException_returns403() throws Exception {
        mockMvc.perform(get("/stub/account-ownership"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Account does not belong to you"));
    }

    @Test
    void accessDeniedException_returns403() throws Exception {
        mockMvc.perform(get("/stub/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }
}