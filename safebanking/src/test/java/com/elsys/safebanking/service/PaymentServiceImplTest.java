package com.elsys.safebanking.service;

import com.elsys.safebanking.config.StripeWebhookVerifier;
import com.elsys.safebanking.dto.TopUpRequest;
import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.StripeDeposit;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.BankAccountRepository;
import com.elsys.safebanking.repository.StripeDepositRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure unit test for {@link PaymentServiceImpl}.
 * No Spring context — all dependencies are Mockito mocks.
 * Real Stripe API calls are never made.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final String IBAN      = "GB29NWBK60161331926819";
    private static final String EMAIL     = "alice@example.com";
    private static final String INTENT_ID = "pi_test_abc123";

    @Mock private BankAccountRepository   bankAccountRepository;
    @Mock private StripeDepositRepository stripeDepositRepository;
    @Mock private StripeWebhookVerifier   webhookVerifier;
    @Mock private StripePaymentClient     stripePaymentClient;

    private PaymentServiceImpl paymentService;

    private User owner;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                bankAccountRepository,
                stripeDepositRepository,
                webhookVerifier,
                stripePaymentClient
        );
        owner   = new User(EMAIL, "hash", "Alice", "Smith");
        account = new BankAccount("Savings", IBAN, new BigDecimal("500.00"), "eur", owner);
    }

    // =========================================================================
    // createIntent — guard-rail paths (happy path requires real Stripe network)
    // =========================================================================

    @Test
    void createIntent_throwsNotFound_whenIbanNotInRepository() {
        when(bankAccountRepository.findByIban(IBAN)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                paymentService.createIntent(new TopUpRequest(IBAN, 1000L, "eur"), EMAIL))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(IBAN);
    }

    @Test
    void createIntent_throwsAccessDenied_whenAccountBelongsToDifferentUser() {
        when(bankAccountRepository.findByIban(IBAN)).thenReturn(Optional.of(account));

        assertThatThrownBy(() ->
                paymentService.createIntent(new TopUpRequest(IBAN, 1000L, "eur"), "other@example.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createIntent_convertsBgnChargeToEurAndPreservesCreditMetadata() throws Exception {
        BankAccount bgnAccount = new BankAccount("Lev account", IBAN, BigDecimal.ZERO, "BGN", owner);
        PaymentIntent createdIntent = mock(PaymentIntent.class);
        when(createdIntent.getClientSecret()).thenReturn("pi_secret");
        when(bankAccountRepository.findByIban(IBAN)).thenReturn(Optional.of(bgnAccount));
        when(stripePaymentClient.create(any())).thenReturn(createdIntent);

        paymentService.createIntent(new TopUpRequest(IBAN, 1956L, "BGN"), EMAIL);

        ArgumentCaptor<PaymentIntentCreateParams> paramsCaptor =
                ArgumentCaptor.forClass(PaymentIntentCreateParams.class);
        verify(stripePaymentClient).create(paramsCaptor.capture());
        PaymentIntentCreateParams params = paramsCaptor.getValue();
        assertThat(params.getAmount()).isEqualTo(1000L);
        assertThat(params.getCurrency()).isEqualTo("eur");
        assertThat(params.getMetadata()).containsEntry("creditAmountCents", "1956");
        assertThat(params.getMetadata()).containsEntry("creditCurrency", "BGN");
    }

    @Test
    void confirmPayment_creditsSucceededIntent() throws Exception {
        PaymentIntent intent = succeededIntent(INTENT_ID, 1000L, IBAN);
        when(intent.getStatus()).thenReturn("succeeded");
        when(stripePaymentClient.retrieve(INTENT_ID)).thenReturn(intent);
        when(stripeDepositRepository.existsByStripePaymentIntentId(INTENT_ID)).thenReturn(false);
        when(bankAccountRepository.findByIban(IBAN)).thenReturn(Optional.of(account));

        paymentService.confirmPayment(INTENT_ID, EMAIL);

        assertThat(account.getBalance()).isEqualByComparingTo("510.00");
        verify(stripeDepositRepository).save(any(StripeDeposit.class));
    }

    @Test
    void confirmPayment_rejectsIncompleteIntent() throws Exception {
        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getStatus()).thenReturn("requires_payment_method");
        when(stripePaymentClient.retrieve(INTENT_ID)).thenReturn(intent);

        assertThatThrownBy(() -> paymentService.confirmPayment(INTENT_ID, EMAIL))
                .isInstanceOf(com.elsys.safebanking.exception.InvalidRequestException.class)
                .hasMessageContaining("has not succeeded");

        verifyNoInteractions(bankAccountRepository, stripeDepositRepository);
    }

    // =========================================================================
    // handleWebhook — signature failure
    // =========================================================================

    @Test
    void handleWebhook_propagatesSignatureVerificationException() throws SignatureVerificationException {
        SignatureVerificationException ex = new SignatureVerificationException("bad sig", "sig-header");
        when(webhookVerifier.constructEvent(any(), any())).thenThrow(ex);

        assertThatThrownBy(() -> paymentService.handleWebhook("payload".getBytes(), "sig"))
                .isSameAs(ex);
    }

    // =========================================================================
    // handleWebhook — non-succeeded event type
    // =========================================================================

    @Test
    void handleWebhook_ignoresNonSucceededEventTypes() throws SignatureVerificationException {
        // Build event BEFORE passing to thenReturn — avoids UnfinishedStubbing
        Event event = eventOfType("payment_intent.created");
        when(webhookVerifier.constructEvent(any(), any())).thenReturn(event);

        paymentService.handleWebhook("payload".getBytes(), "sig");

        verifyNoInteractions(bankAccountRepository, stripeDepositRepository);
    }

    // =========================================================================
    // handleWebhook — API-version fallback deserialization
    // =========================================================================

    @Test
    void handleWebhook_usesUnsafeDeserializer_whenSdkRejectsEventApiVersion() throws Exception {
        PaymentIntent intent = succeededIntent(INTENT_ID, 1000L, IBAN);
        Event event = succeededEventWithFallbackDeserializer(intent);
        when(webhookVerifier.constructEvent(any(), any())).thenReturn(event);
        when(stripeDepositRepository.existsByStripePaymentIntentId(INTENT_ID)).thenReturn(false);
        when(bankAccountRepository.findByIban(IBAN)).thenReturn(Optional.of(account));

        paymentService.handleWebhook("payload".getBytes(), "sig");

        assertThat(account.getBalance()).isEqualByComparingTo("510.00");
        verify(stripeDepositRepository).save(any(StripeDeposit.class));
    }

    // =========================================================================
    // handleWebhook — duplicate idempotency check
    // =========================================================================

    @Test
    void handleWebhook_ignoresDuplicatePaymentIntent() throws SignatureVerificationException {
        Event event = succeededEvent(INTENT_ID, 1000L, IBAN);
        when(webhookVerifier.constructEvent(any(), any())).thenReturn(event);
        when(stripeDepositRepository.existsByStripePaymentIntentId(INTENT_ID)).thenReturn(true);

        paymentService.handleWebhook("payload".getBytes(), "sig");

        verify(stripeDepositRepository, never()).save(any());
        verify(bankAccountRepository, never()).findByIban(any());
    }

    // =========================================================================
    // handleWebhook — account not found
    // =========================================================================

    @Test
    void handleWebhook_throwsNotFound_whenIbanMissingFromRepository() throws SignatureVerificationException {
        Event event = succeededEvent(INTENT_ID, 1000L, IBAN);
        when(webhookVerifier.constructEvent(any(), any())).thenReturn(event);
        when(stripeDepositRepository.existsByStripePaymentIntentId(INTENT_ID)).thenReturn(false);
        when(bankAccountRepository.findByIban(IBAN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.handleWebhook("payload".getBytes(), "sig"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(IBAN);
    }

    // =========================================================================
    // handleWebhook — happy path: balance credited, deposit saved
    // =========================================================================

    @Test
    void handleWebhook_creditsBalanceAndPersistsDeposit_onSucceededEvent() throws SignatureVerificationException {
        Event event = succeededEvent(INTENT_ID, 1000L, IBAN);
        when(webhookVerifier.constructEvent(any(), any())).thenReturn(event);
        when(stripeDepositRepository.existsByStripePaymentIntentId(INTENT_ID)).thenReturn(false);
        when(bankAccountRepository.findByIban(IBAN)).thenReturn(Optional.of(account));
        when(stripeDepositRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        paymentService.handleWebhook("payload".getBytes(), "sig");

        // 500.00 + 10.00 (1000 cents) = 510.00
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("510.00"));
        ArgumentCaptor<StripeDeposit> depositCaptor = ArgumentCaptor.forClass(StripeDeposit.class);
        verify(stripeDepositRepository).save(depositCaptor.capture());
    }

    @Test
    void handleWebhook_convertsCentsToDecimalCorrectly() throws SignatureVerificationException {
        // 999 cents → 9.99
        Event event = succeededEvent(INTENT_ID, 999L, IBAN);
        when(webhookVerifier.constructEvent(any(), any())).thenReturn(event);
        when(stripeDepositRepository.existsByStripePaymentIntentId(INTENT_ID)).thenReturn(false);
        when(bankAccountRepository.findByIban(IBAN)).thenReturn(Optional.of(account));
        when(stripeDepositRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        paymentService.handleWebhook("payload".getBytes(), "sig");

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("509.99"));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Creates mock Event objects OUTSIDE of any Mockito when() call to avoid
     * UnfinishedStubbing errors — building a mock inside thenReturn() confuses
     * Mockito's stubbing state machine.
     */
    private Event eventOfType(String type) {
        Event event = mock(Event.class, withSettings().lenient());
        when(event.getType()).thenReturn(type);
        when(event.getId()).thenReturn("evt_test_001");
        return event;
    }

    private Event succeededEventWithFallbackDeserializer(PaymentIntent intent) throws Exception {
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.empty());
        when(deserializer.deserializeUnsafe()).thenReturn(intent);

        Event event = mock(Event.class, withSettings().lenient());
        when(event.getType()).thenReturn("payment_intent.succeeded");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(event.getId()).thenReturn("evt_test_001");
        return event;
    }

    private Event succeededEvent(String intentId, long amountCents, String iban) {
        PaymentIntent intent = succeededIntent(intentId, amountCents, iban);

        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.of(intent));

        Event event = mock(Event.class, withSettings().lenient());
        when(event.getType()).thenReturn("payment_intent.succeeded");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(event.getId()).thenReturn("evt_test_001");
        return event;
    }

    private PaymentIntent succeededIntent(String intentId, long amountCents, String iban) {
        PaymentIntent intent = mock(PaymentIntent.class, withSettings().lenient());
        when(intent.getId()).thenReturn(intentId);
        when(intent.getAmount()).thenReturn(amountCents);
        when(intent.getCurrency()).thenReturn("eur");
        when(intent.getMetadata()).thenReturn(Map.of("accountIban", iban));
        return intent;
    }
}
