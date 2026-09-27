package com.eve.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentStatusRequest(

        @NotBlank(message = "Payment status is required")
        String status

) {
}