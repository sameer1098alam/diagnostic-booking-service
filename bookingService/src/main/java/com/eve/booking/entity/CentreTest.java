package com.eve.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "centre_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CentreTest {
    @EmbeddedId
    private CentreTestId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("centreId")
    @JoinColumn(name = "centre_id", nullable = false)
    private DiagnosticCentre centre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("testId")
    @JoinColumn(name = "test_id", nullable = false)
    private DiagnosticTest test;

    @Column(nullable = false)
    private BigDecimal price;
}
