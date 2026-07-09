package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.PaymentIntentResponse;
import com.elsys.safebanking.dto.TopUpRequest;
import com.elsys.safebanking.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @Valid @RequestBody TopUpRequest request,
            Principal principal
    ) throws StripeException {
        PaymentIntentResponse response = paymentService.createIntent(request, principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Stripe webhook endpoint. Stripe posts the raw JSON body with a
     * {@code Stripe-Signature} header. We consume it as plain text so Spring
     * does not re-parse the bytes — the SDK needs the exact original payload
     * to verify the HMAC signature.
     *
     * <p>This endpoint is {@code permitAll()} in SecurityConfig; security is
     * provided entirely by signature verification inside the service layer.
     */
    @PostMapping(value = "/webhook", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws SignatureVerificationException {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
