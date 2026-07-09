package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.PaymentIntentResponse;
import com.elsys.safebanking.dto.TopUpRequest;
import com.elsys.safebanking.service.AppUserDetailsService;
import com.elsys.safebanking.service.JwtService;
import com.elsys.safebanking.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WebMvcTest slice for {@link PaymentController}.
 * Security filters are bypassed ({@code addFilters = false}) — the same pattern
 * used in {@code TransactionControllerTests}. Authentication enforcement is
 * covered by the full-stack {@code AuthControllerTests}.
 *
 * <p>This class focuses on: bean-validation rejection, service delegation, and
 * correct HTTP status codes. The happy-path {@code create-intent} flow (which
 * requires a real Stripe network call) is exercised manually with test-mode keys.
 */
@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private PaymentService        paymentService;
    @MockitoBean private AppUserDetailsService appUserDetailsService;
    @MockitoBean private JwtService            jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // POST /api/payments/create-intent — bean validation
    // (happy path omitted: requires real Stripe network call; service is unit-tested in PaymentServiceImplTest)
    // =========================================================================

    @Test
    @WithMockUser
    void createIntent_withMissingIban_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TopUpRequest(null, 1000L, "eur"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.accountIban").exists());
    }

    @Test
    @WithMockUser
    void createIntent_withNullAmount_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TopUpRequest("GB29NWBK60161331926819", null, "eur"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amountCents").exists());
    }

    @Test
    @WithMockUser
    void createIntent_withNegativeAmount_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TopUpRequest("GB29NWBK60161331926819", -100L, "eur"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amountCents").exists());
    }

    @Test
    @WithMockUser
    void createIntent_withMissingCurrency_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TopUpRequest("GB29NWBK60161331926819", 1000L, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.currency").exists());
    }

    // =========================================================================
    // POST /api/payments/webhook
    // =========================================================================

    @Test
    void webhook_withValidSignature_returnsOk() throws Exception {
        doNothing().when(paymentService).handleWebhook(any(), any());

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Stripe-Signature", "t=123,v1=abc")
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void webhook_withInvalidSignature_returnsBadRequest() throws Exception {
        doThrow(new SignatureVerificationException("bad sig", "sig-header"))
                .when(paymentService).handleWebhook(any(), any());

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Stripe-Signature", "t=123,v1=tampered")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Stripe webhook signature"));
    }

    @Test
    void webhook_missingStripeSignatureHeader_returnsBadRequest() throws Exception {
        // No Stripe-Signature header → Spring returns 400 (required header missing)
        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private String validTopUpJson() throws Exception {
        return objectMapper.writeValueAsString(
                new TopUpRequest("GB29NWBK60161331926819", 1000L, "eur"));
    }
}
