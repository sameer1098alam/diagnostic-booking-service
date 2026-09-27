package com.eve.booking.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateCentreRequest {

    @NotBlank(message = "Centre name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}