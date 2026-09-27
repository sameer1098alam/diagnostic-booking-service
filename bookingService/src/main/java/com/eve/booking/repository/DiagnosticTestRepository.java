package com.eve.booking.repository;

import com.eve.booking.entity.DiagnosticTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest, Long> {
}
