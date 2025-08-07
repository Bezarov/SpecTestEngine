package com.example.spectestengine.service;

import com.example.spectestengine.dto.TestRunDTO;
import com.example.spectestengine.engine.TestRunEngine;
import com.example.spectestengine.model.TestRunEntity;
import com.example.spectestengine.repository.TestRunRepository;
import com.example.spectestengine.repository.TestSpecRepository;
import com.example.spectestengine.utils.TestRunMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@Transactional
public class TestRunServiceImpl implements TestRunService {
    private static final String SPEC_NOT_FOUND_LOG_MSG = "Specification not found with '%s': '%s'";

    private final TestSpecRepository testSpecRepository;
    private final TestRunRepository testRunRepository;
    private final TestRunEngine testRunEngine;

    public TestRunServiceImpl(TestSpecRepository testSpecRepository, TestRunRepository testRunRepository, TestRunEngine testRunEngine) {
        this.testSpecRepository = testSpecRepository;
        this.testRunRepository = testRunRepository;
        this.testRunEngine = testRunEngine;
    }

    @Override
    public List<TestRunDTO> runAllTestsSpec() {
        log.info("Running all test specifications");
        return testSpecRepository.findAll().stream()
                .map(testSpecEntity -> {
                    TestRunEntity runEntity = testRunEngine.buildTestRun(testSpecEntity);
                    testRunRepository.save(runEntity);
                    return TestRunMapper.mapToDTO(runEntity);
                })
                .toList();
    }

    @Override
    public TestRunDTO runTestBySpecId(Long specId) {
        log.info("Running test for specification ID: '{}'", specId);
        return testSpecRepository.findById(specId)
                .map(testSpecEntity -> {
                    TestRunEntity runEntity = testRunEngine.buildTestRun(testSpecEntity);
                    testRunRepository.save(runEntity);
                    log.info("Successfully run test id: '{}', for specification ID: '{}'", runEntity.getId(), specId);
                    return TestRunMapper.mapToDTO(runEntity);
                })
                .orElseThrow(() -> {
                    log.warn("Run test failed - specification with ID: '{}' not found", specId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("id:", specId));
                });
    }

    @Override
    public TestRunDTO runTestWithSpecName(String specName) {
        log.info("Running test for specification name: '{}'", specName);
        return testSpecRepository.findByName(specName)
                .map(testSpecEntity -> {
                    TestRunEntity runEntity = testRunEngine.buildTestRun(testSpecEntity);
                    testRunRepository.save(runEntity);
                    log.info("Successfully run test id: '{}', for specification name: '{}'", runEntity.getId(), specName);
                    return TestRunMapper.mapToDTO(runEntity);
                })
                .orElseThrow(() -> {
                    log.warn("Run test failed - specification with name: '{}' not found", specName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("name:", specName));
                });
    }

    @Override
    public List<TestRunDTO> runTestsInSpecRangeId(Long fromId, Long toId) {
        log.info("Running tests for id specification range: '{}' to '{}'", fromId, toId);
        return testSpecRepository.findAllByIdBetween(fromId, toId).stream()
                .map(testSpecEntity -> {
                    TestRunEntity runEntity = testRunEngine.buildTestRun(testSpecEntity);
                    testRunRepository.save(runEntity);
                    log.info("Successfully run test id: '{}', in range specification : '{}' to '{}'", runEntity.getId(), fromId, toId);
                    return TestRunMapper.mapToDTO(runEntity);
                })
                .toList();
    }
}
