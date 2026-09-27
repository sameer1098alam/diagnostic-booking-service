package com.eve.booking.repository;

import com.eve.booking.entity.CentreTest;
import com.eve.booking.entity.CentreTestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CentreTestRepository
        extends JpaRepository<CentreTest, CentreTestId> {

    Optional<CentreTest> findByCentre_IdAndTest_Id(
            Long centreId,
            Long testId
    );

    List<CentreTest> findByCentre_Id(Long centreId);
}