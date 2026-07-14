package com.elsys.safebanking.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Component;

@Component
public class StripePaymentClient {

    public PaymentIntent create(PaymentIntentCreateParams params) throws StripeException {
        return PaymentIntent.create(params);
    }

    public PaymentIntent retrieve(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }
}
