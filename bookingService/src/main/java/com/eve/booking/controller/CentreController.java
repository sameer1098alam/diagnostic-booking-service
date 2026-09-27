package com.eve.booking.controller;

import com.eve.booking.dto.AddTestToCentreRequest;
import com.eve.booking.dto.CentreResponse;
import com.eve.booking.dto.CreateCentreRequest;
import com.eve.booking.service.CentreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centres")
public class CentreController {

    private final CentreService centreService;

    public CentreController(CentreService centreService) {
        this.centreService = centreService;
    }

    @GetMapping
    public ResponseEntity<List<CentreResponse>> getAll() {
        return ResponseEntity.ok(centreService.getAllCentres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentreResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                centreService.getCentreById(id)
        );
    }

    @PostMapping
    public ResponseEntity<CentreResponse> create(
            @Valid @RequestBody CreateCentreRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(centreService.createCentre(request));
    }

    @PostMapping("/{centreId}/tests")
    public ResponseEntity<CentreResponse> addTestToCentre(
            @PathVariable Long centreId,
            @Valid @RequestBody AddTestToCentreRequest request) {

        return ResponseEntity.ok(
                centreService.addTestToCentre(
                        centreId,
                        request
                )
        );
    }
}