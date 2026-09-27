package com.eve.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateBookingRequest(

        @NotNull(message = "Centre ID is required")
        Long centreId,

        @NotNull(message = "Test ID is required")
        Long testId,

        @NotNull(message = "Appointment time is required")
        @Future(message = "Appointment time must be in the future")
        LocalDateTime appointmentTime
) {
}