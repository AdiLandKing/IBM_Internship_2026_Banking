package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.TransferRequest;
import com.elsys.safebanking.dto.TransferResponse;
import com.elsys.safebanking.service.AppUserDetailsService;
import com.elsys.safebanking.service.JwtService;
import com.elsys.safebanking.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void transfer_WithValidRequest_ReturnsOk() throws Exception {
        TransferRequest request = new TransferRequest(
                "BG123456789", "BG987654321", BigDecimal.valueOf(100),
                "Payment", "BGN", "BGN"
        );
        TransferResponse response = new TransferResponse(1L, "PENDING");

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
        // Невалидна заявка: празен IBAN и отрицателна сума
        TransferRequest request = new TransferRequest(
                "", "BG987654321", BigDecimal.valueOf(-50),
                "Payment", "BGN", "BGN"
        );

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.sourceAccountIban").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }
}