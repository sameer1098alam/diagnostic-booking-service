package com.eve.booking.dto;

import java.util.List;

public record CentreResponse(
        Long id,
        String name,
        String location,
        List<TestResponse> tests
) {}
