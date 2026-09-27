package com.eve.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddTestToCentreRequest(

        @NotNull(message = "Test ID is required")
        Long testId,

        @NotNull(message = "Price is required")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "Price must be greater than 0"
        )
        BigDecimal price
) {
}