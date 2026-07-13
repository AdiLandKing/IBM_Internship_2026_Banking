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

    private PaymentServiceImpl paymentService;

    private User owner;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(bankAccountRepository, stripeDepositRepository, webhookVerifier);
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
    // handleWebhook — PaymentIntent deserialization missing
    // =========================================================================

    @Test
    void handleWebhook_returnsEarly_whenDeserializerReturnsEmpty() throws SignatureVerificationException {
        Event event = succeededEventWithEmptyDeserializer();
        when(webhookVerifier.constructEvent(any(), any())).thenReturn(event);

        paymentService.handleWebhook("payload".getBytes(), "sig");

        verifyNoInteractions(bankAccountRepository, stripeDepositRepository);
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

    private Event succeededEventWithEmptyDeserializer() {
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.empty());

        Event event = mock(Event.class, withSettings().lenient());
        when(event.getType()).thenReturn("payment_intent.succeeded");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(event.getId()).thenReturn("evt_test_001");
        return event;
    }

    private Event succeededEvent(String intentId, long amountCents, String iban) {
        PaymentIntent intent = mock(PaymentIntent.class, withSettings().lenient());
        when(intent.getId()).thenReturn(intentId);
        when(intent.getAmount()).thenReturn(amountCents);
        when(intent.getCurrency()).thenReturn("eur");
        when(intent.getMetadata()).thenReturn(Map.of("accountIban", iban));

        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.of(intent));

        Event event = mock(Event.class, withSettings().lenient());
        when(event.getType()).thenReturn("payment_intent.succeeded");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(event.getId()).thenReturn("evt_test_001");
        return event;
    }
}
