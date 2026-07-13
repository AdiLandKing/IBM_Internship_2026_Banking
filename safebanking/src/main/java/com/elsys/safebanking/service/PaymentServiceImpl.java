package com.elsys.safebanking.service;

import com.elsys.safebanking.config.StripeWebhookVerifier;
import com.elsys.safebanking.dto.PaymentIntentResponse;
import com.elsys.safebanking.dto.TopUpRequest;
import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.StripeDeposit;
import com.elsys.safebanking.repository.BankAccountRepository;
import com.elsys.safebanking.repository.StripeDepositRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final String EVENT_PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";

    private final BankAccountRepository bankAccountRepository;
    private final StripeDepositRepository stripeDepositRepository;
    private final StripeWebhookVerifier webhookVerifier;

    public PaymentServiceImpl(BankAccountRepository bankAccountRepository,
                              StripeDepositRepository stripeDepositRepository,
                              StripeWebhookVerifier webhookVerifier) {
        this.bankAccountRepository = bankAccountRepository;
        this.stripeDepositRepository = stripeDepositRepository;
        this.webhookVerifier = webhookVerifier;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentIntentResponse createIntent(TopUpRequest request, String callerEmail) throws StripeException {
        BankAccount account = bankAccountRepository.findByIban(request.accountIban())
                .orElseThrow(() -> new NoSuchElementException(
                        "Account not found: " + request.accountIban()));

        if (!account.getOwner().getEmail().equalsIgnoreCase(callerEmail)) {
            throw new AccessDeniedException("Account does not belong to the authenticated user");
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(request.amountCents())
                .setCurrency(request.currency().toLowerCase())
                .putMetadata("accountIban", request.accountIban())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return new PaymentIntentResponse(intent.getClientSecret());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Signature verification happens first — before any deserialization or business
     * logic — so a tampered or forged payload never reaches the credit path.
     */
    @Override
    @Transactional
    public void handleWebhook(byte[] payload, String sigHeader) throws SignatureVerificationException {
        // 1. Verify signature — throws SignatureVerificationException on mismatch.
        Event event = webhookVerifier.constructEvent(payload, sigHeader);

        // 2. Only act on payment_intent.succeeded; acknowledge everything else silently.
        if (!EVENT_PAYMENT_INTENT_SUCCEEDED.equals(event.getType())) {
            log.debug("Ignoring Stripe event type: {}", event.getType());
            return;
        }

        // 3. Deserialize the embedded PaymentIntent object.
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isEmpty()) {
            log.error("Could not deserialize PaymentIntent from Stripe event {}", event.getId());
            return;
        }
        PaymentIntent intent = (PaymentIntent) deserializer.getObject().get();

        String intentId = intent.getId();
        String iban = intent.getMetadata().get("accountIban");
        long amountCents = intent.getAmount();
        String currency = intent.getCurrency();

        // 4. Idempotency check — the UNIQUE DB constraint is the hard guard;
        //    this query avoids an unnecessary write attempt on known duplicates.
        if (stripeDepositRepository.existsByStripePaymentIntentId(intentId)) {
            log.warn("Duplicate Stripe webhook for PaymentIntent {}; ignoring.", intentId);
            return;
        }

        // 5. Look up the target account.
        BankAccount account = bankAccountRepository.findByIban(iban)
                .orElseThrow(() -> new NoSuchElementException(
                        "Account not found for IBAN from Stripe metadata: " + iban));

        // 6. Convert cents → decimal and credit the balance.
        BigDecimal amount = BigDecimal.valueOf(amountCents).movePointLeft(2);
        account.updateBalance(account.getBalance().add(amount));

        // 7. Persist the deposit record (UNIQUE on intentId prevents double-credit
        //    even under concurrent webhook deliveries).
        stripeDepositRepository.save(
                new StripeDeposit(intentId, account, amount, currency, Instant.now()));

        log.info("Credited {} {} to account {} via Stripe PaymentIntent {}",
                amount, currency.toUpperCase(), iban, intentId);
    }
}
