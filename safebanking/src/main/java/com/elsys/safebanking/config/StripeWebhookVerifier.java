package com.elsys.safebanking.config;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around the static {@link Webhook#constructEvent} call.
 * Extracting it into a Spring bean makes the webhook verification step
 * injectable and mockable in unit tests without needing a real Stripe secret.
 */
@Component
public class StripeWebhookVerifier {

    private final String webhookSecret;

    public StripeWebhookVerifier(@Value("${stripe.webhook-secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    /**
     * Delegates to {@link Webhook#constructEvent}.
     *
     * @throws SignatureVerificationException if the signature does not match
     */
    public Event constructEvent(String payload, String sigHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }
}
