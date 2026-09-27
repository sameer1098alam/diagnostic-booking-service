package com.eve.booking.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentClient {
    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    @Value("${payment.mock.enabled:true}")
    private boolean mockEnabled;

    @Value("${payment.mock.url:http://localhost:8082/payments}")
    private String mockUrl;

    public boolean processPayment(String userId, Long bookingId, BigDecimal amount) {
        if (!mockEnabled) return true;
        log.info("Mock payment to {} for user {} booking {} amount {}", mockUrl, userId, bookingId, amount);
        return true;
    }
}
