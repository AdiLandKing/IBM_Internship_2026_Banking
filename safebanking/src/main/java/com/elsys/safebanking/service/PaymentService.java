package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.PaymentIntentResponse;
import com.elsys.safebanking.dto.TopUpRequest;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;

public interface PaymentService {

    /**
     * Creates a Stripe PaymentIntent for a top-up and returns the client secret
     * that the frontend uses to confirm the payment.
     *
     * @param request     the top-up details (IBAN, amount in cents, currency)
     * @param callerEmail the authenticated user's email (from JWT principal)
     * @return a {@link PaymentIntentResponse} containing the Stripe client secret
     * @throws StripeException if the Stripe API call fails
     */
    PaymentIntentResponse createIntent(TopUpRequest request, String callerEmail) throws StripeException;

    /**
     * Retrieves a PaymentIntent from Stripe and credits its target account when
     * Stripe confirms that payment succeeded. This is an authenticated fallback
     * for local development, where Stripe cannot deliver webhooks to localhost.
     */
    void confirmPayment(String paymentIntentId, String callerEmail) throws StripeException;

    /**
     * Verifies the Stripe webhook signature and, for {@code payment_intent.succeeded}
     * events, credits the target account and records a {@link com.elsys.safebanking.model.StripeDeposit}.
     * All other event types are acknowledged and ignored.
     *
     * <p><strong>Security:</strong> signature verification is performed before any
     * business logic — skipping it would allow anyone to POST a fake
     * {@code payment_intent.succeeded} event and top up accounts for free.
     *
     * @param payload      raw request body exactly as received (must not be reparsed)
     * @param sigHeader    value of the {@code Stripe-Signature} HTTP header
     * @throws SignatureVerificationException if the signature is invalid or the payload was tampered with
     */
    void handleWebhook(byte[] payload, String sigHeader) throws SignatureVerificationException;
}
