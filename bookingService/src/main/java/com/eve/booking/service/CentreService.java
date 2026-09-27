package com.eve.booking.service;

import com.eve.booking.dto.AddTestToCentreRequest;
import com.eve.booking.dto.CentreResponse;
import com.eve.booking.dto.CreateCentreRequest;
import com.eve.booking.dto.TestResponse;
import com.eve.booking.entity.CentreTest;
import com.eve.booking.entity.CentreTestId;
import com.eve.booking.entity.DiagnosticCentre;
import com.eve.booking.entity.DiagnosticTest;
import com.eve.booking.repository.CentreTestRepository;
import com.eve.booking.repository.DiagnosticCentreRepository;
import com.eve.booking.repository.DiagnosticTestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CentreService {

    private final DiagnosticCentreRepository centreRepository;
    private final CentreTestRepository centreTestRepository;
    private final DiagnosticTestRepository testRepository;

    public CentreService(
            DiagnosticCentreRepository centreRepository,
            CentreTestRepository centreTestRepository,
            DiagnosticTestRepository testRepository) {

        this.centreRepository = centreRepository;
        this.centreTestRepository = centreTestRepository;
        this.testRepository = testRepository;
    }

    @Transactional
    public CentreResponse createCentre(CreateCentreRequest request) {

        DiagnosticCentre centre = new DiagnosticCentre();

        centre.setName(request.getName());
        centre.setLocation(request.getLocation());

        DiagnosticCentre savedCentre = centreRepository.save(centre);

        return new CentreResponse(
                savedCentre.getId(),
                savedCentre.getName(),
                savedCentre.getLocation(),
                List.of()
        );
    }

    @Transactional
    public CentreResponse addTestToCentre(
            Long centreId,
            AddTestToCentreRequest request) {

        DiagnosticCentre centre = centreRepository.findById(centreId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Centre not found: " + centreId));

        DiagnosticTest test = testRepository.findById(request.testId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Test not found: " + request.testId()));

        CentreTestId centreTestId =
                new CentreTestId(centreId, request.testId());

        if (centreTestRepository.existsById(centreTestId)) {
            throw new IllegalArgumentException(
                    "Test is already available at this centre");
        }

        CentreTest centreTest = new CentreTest();

        centreTest.setId(centreTestId);
        centreTest.setCentre(centre);
        centreTest.setTest(test);
        centreTest.setPrice(request.price());

        centreTestRepository.save(centreTest);

        return map(centre);
    }

    @Transactional(readOnly = true)
    public List<CentreResponse> getAllCentres() {

        return centreRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public CentreResponse getCentreById(Long id) {

        DiagnosticCentre c = centreRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Centre not found: " + id));

        return map(c);
    }

    private CentreResponse map(DiagnosticCentre c) {

        var tests = centreTestRepository
                .findByCentre_Id(c.getId())
                .stream()
                .map(this::mapTest)
                .toList();

        return new CentreResponse(
                c.getId(),
                c.getName(),
                c.getLocation(),
                tests
        );
    }

    private TestResponse mapTest(CentreTest ct) {

        return new TestResponse(
                ct.getTest().getId(),
                ct.getTest().getName(),
                ct.getTest().getDescription(),
                ct.getPrice()
        );
    }
}