package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.StripeDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeDepositRepository extends JpaRepository<StripeDeposit, Long> {

    boolean existsByStripePaymentIntentId(String stripePaymentIntentId);
}
