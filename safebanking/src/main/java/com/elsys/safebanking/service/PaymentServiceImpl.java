package com.elsys.safebanking.service;

import com.elsys.safebanking.config.StripeWebhookVerifier;
import com.elsys.safebanking.dto.PaymentIntentResponse;
import com.elsys.safebanking.dto.TopUpRequest;
import com.elsys.safebanking.exception.InvalidRequestException;
import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.StripeDeposit;
import com.elsys.safebanking.repository.BankAccountRepository;
import com.elsys.safebanking.repository.StripeDepositRepository;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final String EVENT_PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";
    private static final String PAYMENT_STATUS_SUCCEEDED = "succeeded";
    private static final String BGN = "BGN";
    private static final String EUR = "EUR";
    private static final BigDecimal BGN_PER_EUR = new BigDecimal("1.95583");
    private static final String METADATA_ACCOUNT_IBAN = "accountIban";
    private static final String METADATA_CREDIT_AMOUNT_CENTS = "creditAmountCents";
    private static final String METADATA_CREDIT_CURRENCY = "creditCurrency";

    private final BankAccountRepository bankAccountRepository;
    private final StripeDepositRepository stripeDepositRepository;
    private final StripeWebhookVerifier webhookVerifier;
    private final StripePaymentClient stripePaymentClient;

    public PaymentServiceImpl(BankAccountRepository bankAccountRepository,
                              StripeDepositRepository stripeDepositRepository,
                              StripeWebhookVerifier webhookVerifier,
                              StripePaymentClient stripePaymentClient) {
        this.bankAccountRepository = bankAccountRepository;
        this.stripeDepositRepository = stripeDepositRepository;
        this.webhookVerifier = webhookVerifier;
        this.stripePaymentClient = stripePaymentClient;
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

        String accountCurrency = account.getCurrency().toUpperCase(Locale.ROOT);
        if (!accountCurrency.equals(request.currency().toUpperCase(Locale.ROOT))) {
            throw new InvalidRequestException("Top-up currency must match the account currency");
        }

        StripeCharge stripeCharge = getStripeCharge(request.amountCents(), accountCurrency);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(stripeCharge.amountCents())
                .setCurrency(stripeCharge.currency().toLowerCase(Locale.ROOT))
                .putMetadata(METADATA_ACCOUNT_IBAN, request.accountIban())
                .putMetadata(METADATA_CREDIT_AMOUNT_CENTS, request.amountCents().toString())
                .putMetadata(METADATA_CREDIT_CURRENCY, accountCurrency)
                .build();

        PaymentIntent intent = stripePaymentClient.create(params);
        return new PaymentIntentResponse(intent.getClientSecret());
    }

    @Override
    @Transactional
    public void confirmPayment(String paymentIntentId, String callerEmail) throws StripeException {
        PaymentIntent intent = stripePaymentClient.retrieve(paymentIntentId);
        if (!PAYMENT_STATUS_SUCCEEDED.equals(intent.getStatus())) {
            throw new InvalidRequestException("Stripe payment has not succeeded");
        }

        creditSucceededIntent(intent, callerEmail);
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

        creditSucceededIntent(deserializePaymentIntent(event), null);
    }

    private PaymentIntent deserializePaymentIntent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = deserializer.getObject().orElse(null);

        if (stripeObject == null) {
            try {
                // Stripe returns an empty Optional when the event API version differs
                // from the SDK version. The signature was already verified above.
                stripeObject = deserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException exception) {
                throw new IllegalStateException(
                        "Could not deserialize PaymentIntent from Stripe event " + event.getId(),
                        exception);
            }
        }

        if (!(stripeObject instanceof PaymentIntent paymentIntent)) {
            throw new IllegalStateException(
                    "Stripe event " + event.getId() + " does not contain a PaymentIntent");
        }

        return paymentIntent;
    }

    private void creditSucceededIntent(PaymentIntent intent, String callerEmail) {
        String intentId = intent.getId();

        if (stripeDepositRepository.existsByStripePaymentIntentId(intentId)) {
            log.warn("Duplicate Stripe webhook for PaymentIntent {}; ignoring.", intentId);
            return;
        }

        Map<String, String> metadata = intent.getMetadata();
        String iban = metadata.get(METADATA_ACCOUNT_IBAN);
        if (iban == null || iban.isBlank()) {
            throw new InvalidRequestException("Stripe payment is missing its target account");
        }

        BankAccount account = bankAccountRepository.findByIban(iban)
                .orElseThrow(() -> new NoSuchElementException(
                        "Account not found for IBAN from Stripe metadata: " + iban));

        if (callerEmail != null && !account.getOwner().getEmail().equalsIgnoreCase(callerEmail)) {
            throw new AccessDeniedException("Payment does not belong to the authenticated user");
        }

        long amountCents = getCreditAmountCents(intent, metadata);
        String currency = metadata.getOrDefault(METADATA_CREDIT_CURRENCY, intent.getCurrency())
                .toUpperCase(Locale.ROOT);
        if (!account.getCurrency().equalsIgnoreCase(currency)) {
            throw new InvalidRequestException("Stripe payment currency does not match the account currency");
        }

        BigDecimal amount = BigDecimal.valueOf(amountCents).movePointLeft(2);
        account.updateBalance(account.getBalance().add(amount));

        stripeDepositRepository.save(
                new StripeDeposit(intentId, account, amount, currency, Instant.now()));

        log.info("Credited {} {} to account {} via Stripe PaymentIntent {}",
                amount, currency.toUpperCase(), iban, intentId);
    }

    private long getCreditAmountCents(PaymentIntent intent, Map<String, String> metadata) {
        String configuredAmount = metadata.get(METADATA_CREDIT_AMOUNT_CENTS);
        if (configuredAmount == null) {
            return intent.getAmount();
        }

        try {
            long amountCents = Long.parseLong(configuredAmount);
            if (amountCents <= 0) {
                throw new NumberFormatException("Amount must be positive");
            }
            return amountCents;
        } catch (NumberFormatException exception) {
            throw new InvalidRequestException("Stripe payment contains an invalid credit amount");
        }
    }

    private StripeCharge getStripeCharge(long creditAmountCents, String accountCurrency) {
        if (!BGN.equals(accountCurrency)) {
            return new StripeCharge(creditAmountCents, accountCurrency);
        }

        long euroAmountCents = BigDecimal.valueOf(creditAmountCents)
                .divide(BGN_PER_EUR, 0, RoundingMode.HALF_UP)
                .max(BigDecimal.ONE)
                .longValueExact();
        return new StripeCharge(euroAmountCents, EUR);
    }

    private record StripeCharge(long amountCents, String currency) {
    }
}
