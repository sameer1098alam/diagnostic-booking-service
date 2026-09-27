package com.eve.booking.service;

import com.eve.booking.dto.CreateTestRequest;
import com.eve.booking.dto.TestResponse;
import com.eve.booking.entity.DiagnosticTest;
import com.eve.booking.repository.DiagnosticTestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TestService {

    private final DiagnosticTestRepository testRepository;

    public TestService(DiagnosticTestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @Transactional
    public TestResponse createTest(CreateTestRequest request) {

        DiagnosticTest test = new DiagnosticTest();
        test.setName(request.getName());
        test.setDescription(request.getDescription());

        DiagnosticTest saved = testRepository.save(test);

        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<TestResponse> getAllTests() {
        return testRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public TestResponse getTestById(Long id) {
        DiagnosticTest test = testRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Test not found: " + id));

        return map(test);
    }

    private TestResponse map(DiagnosticTest test) {
        return new TestResponse(
                test.getId(),
                test.getName(),
                test.getDescription(),
                null
        );
    }
}