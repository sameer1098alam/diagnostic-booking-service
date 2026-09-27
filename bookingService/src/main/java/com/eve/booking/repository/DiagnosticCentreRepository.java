package com.eve.booking.repository;

import com.eve.booking.entity.DiagnosticCentre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosticCentreRepository extends JpaRepository<DiagnosticCentre, Long> {
}
