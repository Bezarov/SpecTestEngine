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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HTTPTestSpecServiceImpl implements HTTPTestSpecService {
    private static final String SPEC_NOT_FOUND_LOG = "Specification not found with ";
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
        testSpecRepository.findByName(specName)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Specification with name '" + specName + "' already exists");
                });

        TestSpecEntity entity = TestSpecEntity.builder()
                .name(specName)
                .spec(specJson)
                .createdAt(LocalDateTime.now())
                .build();

        return SpecMapper.mapToDTO(testSpecRepository.save(entity));
    }

    @Override
    public TestSpecDTO getSpecById(Long specId) {
        return testSpecRepository.findById(specId)
                .map(SpecMapper::mapToDTO)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, SPEC_NOT_FOUND_LOG + "id: " + specId));
    }

    @Override
    public List<TestSpecDTO> getAllTestSpec() {
        return testSpecRepository.findAll().stream()
                .map(SpecMapper::mapToDTO)
                .toList();
    }

    @Override
    public List<TestRunResultDTO> runAllTestsSpec() {
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
        return testSpecRepository.findById(specId)
                .map(testSpecEntity -> {
                    TestRunEntity runEntity = testRunEngine.buildTestRun(testSpecEntity);
                    testRunRepository.save(runEntity);
                    return TestRunMapper.mapToDTO(runEntity);
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + "id: " + specId));
    }

    @Override
    public TestRunResultDTO runTestWithSpecName(String specName) {
        return testSpecRepository.findByName(specName)
                .map(testSpecEntity -> {
                    TestRunEntity runEntity = testRunEngine.buildTestRun(testSpecEntity);
                    testRunRepository.save(runEntity);
                    return TestRunMapper.mapToDTO(runEntity);
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + "name: " + specName));
    }

    @Override
    public List<TestRunResultDTO> runTestsInSpecRangeId(Long fromId, Long toId) {
        return testSpecRepository.findAllByIdBetween(fromId, toId).stream()
                .map(testSpecEntity -> {
                    TestRunEntity runEntity = testRunEngine.buildTestRun(testSpecEntity);
                    testRunRepository.save(runEntity);
                    return TestRunMapper.mapToDTO(runEntity);
                })
                .toList();
    }

    @Override
    public TestSpecWithRunsDTO getSpecWithRuns(Long specId) {
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
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + "id: " + specId));
    }

    @Override
    public TestSpecDTO updateSpecById(Long specId, String specJson) {
        return testSpecRepository.findById(specId)
                .map(spec -> {
                    spec.setSpec(specJson);
                    return SpecMapper.mapToDTO(testSpecRepository.save(spec));
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + "id: " + specId));
    }

    @Override
    public TestSpecDTO updateSpecByName(String specName, String specJson) {
        return testSpecRepository.findByName(specName)
                .map(spec -> {
                    spec.setSpec(specJson);
                    return SpecMapper.mapToDTO(testSpecRepository.save(spec));
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + "name: " + specName));
    }

    @Override
    public TestSpecDTO deleteSpecById(Long specId, String specJson) {
        return testSpecRepository.findById(specId)
                .map(spec -> {
                    testSpecRepository.delete(spec);
                    return SpecMapper.mapToDTO(spec);
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + "id: " + specId));
    }

    @Override
    public TestSpecDTO deleteSpecByName(String specName, String specJson) {
        return testSpecRepository.findByName(specName)
                .map(spec -> {
                    testSpecRepository.delete(spec);
                    return SpecMapper.mapToDTO(spec);
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + "name: " + specName));
    }
}