package com.eve.booking.controller;

import com.eve.booking.dto.CreateTestRequest;
import com.eve.booking.dto.TestResponse;
import com.eve.booking.service.TestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @PostMapping
    public ResponseEntity<TestResponse> create(
            @Valid @RequestBody CreateTestRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(testService.createTest(request));
    }

    @GetMapping
    public ResponseEntity<List<TestResponse>> getAll() {
        return ResponseEntity.ok(testService.getAllTests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(testService.getTestById(id));
    }
}