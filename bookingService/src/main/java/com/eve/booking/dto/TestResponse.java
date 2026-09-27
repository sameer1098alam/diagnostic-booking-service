package com.eve.booking.dto;

import java.math.BigDecimal;

public record TestResponse(
        Long id,
        String name,
        String description,
        BigDecimal price
) {}
