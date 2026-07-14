package com.elsys.safebanking.config;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Thin wrapper around the static {@link Webhook#constructEvent} call.
 * Extracting it into a Spring bean makes the webhook verification step
 * injectable and mockable in unit tests without needing a real Stripe secret.
 */
@Component
public class StripeWebhookVerifier {

    private final String webhookSecret;

    public StripeWebhookVerifier(Environment environment) {
        this.webhookSecret = environment.getProperty("STRIPE_WEBHOOK_SECRET", "");
    }

    /**
     * Delegates to {@link Webhook#constructEvent}.
     *
     * @throws SignatureVerificationException if the signature does not match
     */
    public Event constructEvent(byte[] payload, String sigHeader) throws SignatureVerificationException {
        String decodedPayload = new String(payload, StandardCharsets.UTF_8);
        return Webhook.constructEvent(decodedPayload, sigHeader, webhookSecret);
    }
}
