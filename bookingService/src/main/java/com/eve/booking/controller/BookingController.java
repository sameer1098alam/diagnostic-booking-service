package com.eve.booking.controller;

import com.eve.booking.dto.BookingResponse;
import com.eve.booking.dto.CreateBookingRequest;
import com.eve.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    @Value("${payment.service-key}")
    private String paymentServiceKey;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody CreateBookingRequest req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookingService.createBooking(req));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> get(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                bookingService.getBookingById(id)
        );
    }

    @GetMapping("/users/{userId}/bookings")
    public ResponseEntity<List<BookingResponse>> listByUser(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                bookingService.getBookingsByUserId(userId)
        );
    }

    /*
     * Internal service-to-service endpoint.
     * Payment Service uses X-Service-Key instead of JWT.
     */
    @PutMapping("/bookings/{id}/payment-status")
    public ResponseEntity<Void> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Service-Key", required = false)
            String serviceKey) {

        // Verify Payment Service
        if (!paymentServiceKey.equals(serviceKey)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        bookingService.updatePaymentStatus(
                id,
                request.get("status")
        );

        return ResponseEntity.noContent().build();
    }
}