package com.example.spectestengine.service;

import com.example.spectestengine.dto.TestRunDTO;
import com.example.spectestengine.dto.TestRunResultDTO;
import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.dto.TestSpecWithRunsDTO;
import com.example.spectestengine.engine.TestRunEngine;
import com.example.spectestengine.model.TestRunEntity;
import com.example.spectestengine.model.TestSpecEntity;
import com.example.spectestengine.repository.TestRunRepository;
import com.example.spectestengine.repository.TestSpecRepository;
import com.example.spectestengine.utils.JsonMapper;
import com.example.spectestengine.utils.SpecMapper;
import com.example.spectestengine.utils.TestRunMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class HTTPTestSpecServiceImpl implements HTTPTestSpecService {
    private static final String SPEC_NOT_FOUND_LOG_MSG = "Specification not found with '%s': '%s'";

    private final TestSpecRepository testSpecRepository;
    private final TestRunRepository testRunRepository;
    private final TestRunEngine testRunEngine;

    public HTTPTestSpecServiceImpl(TestSpecRepository testSpecRepository, TestRunRepository testRunRepository, TestRunEngine testRunEngine) {
        this.testSpecRepository = testSpecRepository;
        this.testRunRepository = testRunRepository;
        this.testRunEngine = testRunEngine;
    }

    @Override
    public TestSpecDTO createSpec(String specName, String specJson) {
        log.info("Creating new test specification with name: '{}'", specName);
        testSpecRepository.findByName(specName)
                .ifPresent(existing -> {
                    log.warn("Specification with name: '{}', already exists", specName);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Specification with name '%s' already exists".formatted(specName));
                });

        TestSpecEntity savedEntity = testSpecRepository.save(TestSpecEntity.builder()
                .name(specName)
                .spec(specJson)
                .createdAt(LocalDateTime.now())
                .build());
        log.info("Successfully created specification with name: '{}' and ID: '{}'", specName, savedEntity.getId());

        return SpecMapper.mapToDTO(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public TestSpecDTO getSpecById(Long specId) {
        log.debug("Searching specification by ID: '{}'", specId);
        return testSpecRepository.findById(specId)
                .map(SpecMapper::mapToDTO)
                .orElseThrow(() -> {
                    log.warn("Specification not found with ID: '{}'", specId);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("id:", specId));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public TestSpecDTO getSpecByName(String specName) {
        log.debug("Searching specification by name: '{}'", specName);
        return testSpecRepository.findByName(specName)
                .map(SpecMapper::mapToDTO)
                .orElseThrow(() -> {
                    log.warn("Specification not found with name: '{}'", specName);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("name:", specName));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestSpecDTO> getAllTestSpec() {
        log.debug("Searching all specifications");
        return testSpecRepository.findAll().stream()
                .map(SpecMapper::mapToDTO)
                .toList();
    }

    @Override
    public List<TestRunResultDTO> runAllTestsSpec() {
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
    public TestRunResultDTO runTestBySpecId(Long specId) {
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
    public TestRunResultDTO runTestWithSpecName(String specName) {
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
    public List<TestRunResultDTO> runTestsInSpecRangeId(Long fromId, Long toId) {
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

    @Override
    @Transactional(readOnly = true)
    public TestSpecWithRunsDTO getSpecWithRuns(Long specId) {
        log.debug("Searching specification with runs for specification ID: '{}'", specId);
        return testSpecRepository.findByIdWithRuns(specId)
                .map(spec -> new TestSpecWithRunsDTO(
                        spec.getId(),
                        spec.getName(),
                        JsonMapper.fromJson(spec.getSpec()),
                        spec.getCreatedAt(),
                        spec.getRuns().stream()
                                .map(run -> new TestRunDTO(
                                        run.getId(),
                                        run.getStatus(),
                                        JsonMapper.fromJson(run.getLog()),
                                        run.getStartedAt(),
                                        run.getFinishedAt()
                                ))
                                .toList()
                ))
                .orElseThrow(() -> {
                    log.warn("Specification with runs not found for ID: '{}'", specId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("id:", specId));
                });
    }

    @Override
    public TestSpecDTO updateSpecById(Long specId, String specJson) {
        log.info("Updating specification with ID: '{}'", specId);
        return testSpecRepository.findById(specId)
                .map(specEntity -> {
                    specEntity.setSpec(specJson);
                    TestSpecEntity savedSpec = testSpecRepository.save(specEntity);
                    log.debug("Specification updated from: '{}' to: '{}'", specEntity, savedSpec);
                    return SpecMapper.mapToDTO(savedSpec);
                })
                .orElseThrow(() -> {
                    log.warn("Update specification failed - specification with id: '{}' not found", specId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("id:", specId));
                });
    }

    @Override
    public TestSpecDTO updateSpecByName(String specName, String specJson) {
        log.info("Updating specification with name: '{}'", specName);
        return testSpecRepository.findByName(specName)
                .map(specEntity -> {
                    specEntity.setSpec(specJson);
                    TestSpecEntity savedSpec = testSpecRepository.save(specEntity);
                    log.debug("Specification updated from: '{}' to: '{}'", specEntity, savedSpec);
                    return SpecMapper.mapToDTO(savedSpec);
                })
                .orElseThrow(() -> {
                    log.warn("Update specification failed - specification with name: '{}' not found", specName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("name:", specName));
                });
    }

    @Override
    public TestSpecDTO deleteSpecById(Long specId) {
        log.info("Deleting specification with id: '{}'", specId);
        return testSpecRepository.findById(specId)
                .map(specEntity -> {
                    testSpecRepository.delete(specEntity);
                    log.debug("Successfully deleted specification with id: '{}'", specId);
                    return SpecMapper.mapToDTO(specEntity);
                })
                .orElseThrow(() -> {
                    log.warn("Delete specification failed - specification with id: '{}' not found", specId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("id:", specId));
                });
    }

    @Override
    public TestSpecDTO deleteSpecByName(String specName) {
        log.info("Deleting specification with name: '{}'", specName);
        return testSpecRepository.findByName(specName)
                .map(specEntity -> {
                    testSpecRepository.delete(specEntity);
                    log.debug("Successfully deleted specification with name: '{}'", specName);
                    return SpecMapper.mapToDTO(specEntity);
                })
                .orElseThrow(() -> {
                    log.warn("Delete specification failed - specification with name: '{}' not found", specName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("name:", specName));
                });
    }
}