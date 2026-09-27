package com.eve.payment.dto;

import com.eve.payment.entity.Payment;
import com.eve.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentResponse {
    private Long id;
    private String bookingId;
    private String providerPaymentId;
    private String eventId;
    private BigDecimal amount;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static PaymentResponse fromEntity(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.id = p.getId();
        r.bookingId = p.getBookingId();
        r.providerPaymentId = p.getProviderPaymentId();
        r.eventId = p.getEventId();
        r.amount = p.getAmount();
        r.status = p.getStatus();
        r.createdAt = p.getCreatedAt();
        r.updatedAt = p.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getBookingId() { return bookingId; }
    public String getProviderPaymentId() { return providerPaymentId; }
    public String getEventId() { return eventId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
