package com.eve.payment.client;

import com.eve.payment.enums.PaymentStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class BookingClient {

    private final RestTemplate restTemplate;

    public BookingClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void notifyBooking(String bookingId, PaymentStatus status) {

        String url = "http://localhost:8082/api/bookings/"
                + bookingId
                + "/payment-status";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Service-to-service authentication
        headers.set("X-Service-Key", "payment-service-secret");

        Map<String, String> body = Map.of(
                "status", status.name()
        );

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(body, headers);

        restTemplate.put(url, request);
    }
}
