package com.eve.payment.repository;

import com.eve.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByEventId(String eventId);
    Optional<Payment> findByProviderPaymentId(String providerPaymentId);
}
