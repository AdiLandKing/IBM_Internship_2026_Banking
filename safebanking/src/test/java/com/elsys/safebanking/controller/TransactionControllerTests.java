package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.TransferRequest;
import com.elsys.safebanking.dto.TransferResponse;
import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.BankingTransaction;
import com.elsys.safebanking.model.TransactionStatus;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.BankingTransactionRepository;
import com.elsys.safebanking.repository.UserRepository;
import com.elsys.safebanking.service.AppUserDetailsService;
import com.elsys.safebanking.service.JwtService;
import com.elsys.safebanking.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @MockitoBean
    private BankingTransactionRepository transactionRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getHistory_ReturnsCurrentUserTransactionsNewestFirst() throws Exception {
        User user = new User("client@example.com", "hash", "Test", "Client");
        user.setId(42L);
        User recipient = new User("recipient@example.com", "hash", "Other", "Client");
        BankAccount source = new BankAccount("BG11SAFE00000000000001", "Main", "EUR", user);
        BankAccount destination = new BankAccount("BG11SAFE00000000000002", "Savings", "EUR", recipient);
        Instant timestamp = Instant.parse("2026-07-14T07:00:00Z");
        BankingTransaction transaction = BankingTransaction.builder()
                .tranId(7L)
                .sourceAccount(source)
                .destinationAccount(destination)
                .amount(new BigDecimal("125.50"))
                .creditedAmount(new BigDecimal("125.50"))
                .sourceCurrency("EUR")
                .destinationCurrency("EUR")
                .reason("Invoice 104")
                .status(TransactionStatus.COMPLETED)
                .timeStamp(timestamp)
                .build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findAllUserTransactions(eq(42L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(transaction)));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", null)
        );

        try {
            mockMvc.perform(get("/api/v1/transactions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].transactionId").value(7))
                    .andExpect(jsonPath("$.content[0].sourceIban").value(source.getIban()))
                    .andExpect(jsonPath("$.content[0].destinationIban").value(destination.getIban()))
                    .andExpect(jsonPath("$.content[0].amount").value(125.50))
                    .andExpect(jsonPath("$.content[0].creditedAmount").value(125.50))
                    .andExpect(jsonPath("$.content[0].sourceCurrency").value("EUR"))
                    .andExpect(jsonPath("$.content[0].destinationCurrency").value("EUR"))
                    .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                    .andExpect(jsonPath("$.content[0].timestamp").value(timestamp.toString()));

            var pageable = org.mockito.ArgumentCaptor.forClass(Pageable.class);
            org.mockito.Mockito.verify(transactionRepository)
                    .findAllUserTransactions(eq(42L), pageable.capture());
            assertThat(pageable.getValue().getPageSize()).isEqualTo(30);
            assertThat(pageable.getValue().getSort().getOrderFor("timeStamp"))
                    .isNotNull()
                    .extracting(org.springframework.data.domain.Sort.Order::getDirection)
                    .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void transfer_WithValidRequest_ReturnsOk() throws Exception {
        TransferRequest request = new TransferRequest(
                "BG123456789", "BG987654321", BigDecimal.valueOf(100),
                "Payment"
        );
        TransferResponse response = new TransferResponse(1L, TransactionStatus.PENDING);
        
        when(transferService.transfer(any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void transfer_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        TransferRequest request = new TransferRequest(
                "", "BG987654321", BigDecimal.valueOf(-50),
                "Payment"
        );

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.sourceAccountIban").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }
}
