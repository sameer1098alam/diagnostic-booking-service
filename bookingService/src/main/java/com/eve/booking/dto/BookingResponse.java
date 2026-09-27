package com.eve.booking.dto;

import com.eve.booking.entity.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        String userId,
        Long centreId,
        String centreName,
        Long testId,
        String testName,
        LocalDateTime bookingDateTime,
        String status,
        BigDecimal amount
) {
    public static BookingResponse fromEntity(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getUserId(),
                b.getCentre().getId(),
                b.getCentre().getName(),
                b.getTest().getId(),
                b.getTest().getName(),
                b.getBookingDateTime(),
                b.getStatus().name(),
                b.getAmount()
        );
    }
}
