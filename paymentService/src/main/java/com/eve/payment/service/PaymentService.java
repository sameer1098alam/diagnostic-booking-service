package com.eve.payment.service;

import com.eve.payment.client.BookingClient;
import com.eve.payment.dto.PaymentRequest;
import com.eve.payment.dto.PaymentResponse;
import com.eve.payment.dto.WebhookRequest;
import com.eve.payment.entity.Payment;
import com.eve.payment.enums.PaymentStatus;
import com.eve.payment.exception.PaymentNotFoundException;
import com.eve.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final BookingClient bookingClient;

    public PaymentService(
            PaymentRepository repository,
            BookingClient bookingClient) {

        this.repository = repository;
        this.bookingClient = bookingClient;
    }

    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {

        Payment payment = new Payment();

        payment.setAmount(request.getAmount());
        payment.setBookingId(request.getBookingId());
        payment.setProviderPaymentId(
                request.getProviderPaymentId()
        );

        // Simulated payment result
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment saved = repository.save(payment);

        // Notify Booking Service
        bookingClient.notifyBooking(
                saved.getBookingId(),
                saved.getStatus()
        );

        return PaymentResponse.fromEntity(saved);
    }

    // =========================================================
    // GET PAYMENT BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {

        Payment payment = repository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(id)
                );

        return PaymentResponse.fromEntity(payment);
    }

    // =========================================================
    // GET ALL PAYMENTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {

        return repository.findAll()
                .stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    // =========================================================
    // HANDLE WEBHOOK
    // =========================================================

    @Transactional
    public void handleWebhook(WebhookRequest request) {

        // -----------------------------------------------------
        // Idempotency check using event ID
        // -----------------------------------------------------

        if (request.getEventId() != null) {

            var existing = repository
                    .findByEventId(request.getEventId());

            if (existing.isPresent()) {
                return;
            }
        }

        Payment payment = null;

        // -----------------------------------------------------
        // Find payment using provider payment ID
        // -----------------------------------------------------

        if (request.getProviderPaymentId() != null) {

            payment = repository
                    .findByProviderPaymentId(
                            request.getProviderPaymentId()
                    )
                    .orElse(null);
        }

        // -----------------------------------------------------
        // Otherwise find payment using payment ID
        // -----------------------------------------------------

        if (payment == null &&
                request.getPaymentId() != null) {

            payment = repository
                    .findById(request.getPaymentId())
                    .orElseThrow(() ->
                            new PaymentNotFoundException(
                                    request.getPaymentId()
                            )
                    );
        }

        // -----------------------------------------------------
        // Payment not found
        // -----------------------------------------------------

        if (payment == null) {

            throw new PaymentNotFoundException(
                    request.getPaymentId()
            );
        }

        // -----------------------------------------------------
        // Update payment status
        // -----------------------------------------------------

        payment.setStatus(request.getStatus());

        // -----------------------------------------------------
        // Store webhook event ID
        // -----------------------------------------------------

        if (request.getEventId() != null) {

            payment.setEventId(
                    request.getEventId()
            );
        }

        Payment saved = repository.save(payment);

        // -----------------------------------------------------
        // Notify Booking Service
        // SUCCESS -> CONFIRMED
        // FAILED  -> FAILED
        // -----------------------------------------------------

        bookingClient.notifyBooking(
                saved.getBookingId(),
                saved.getStatus()
        );
    }
}