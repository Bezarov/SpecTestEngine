package com.example.spectestengine.service;

import com.example.spectestengine.dto.TestRunDTO;
import com.example.spectestengine.dto.TestRunResultDTO;
import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.dto.TestSpecWithRunsDTO;
import com.example.spectestengine.model.TestRunEntity;
import com.example.spectestengine.model.TestSpecEntity;
import com.example.spectestengine.repository.TestRunRepository;
import com.example.spectestengine.repository.TestSpecRepository;
import com.example.spectestengine.utils.SpecMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HTTPTestSpecServiceImpl implements HTTPTestSpecService {
    private static final String SPEC_NOT_FOUND_LOG = "Spec not found with";
    private final TestSpecRepository testSpecRepository;
    private final TestRunRepository testRunRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HTTPTestSpecServiceImpl(TestSpecRepository testSpecRepository, TestRunRepository testRunRepository) {
        this.testSpecRepository = testSpecRepository;
        this.testRunRepository = testRunRepository;
    }

    @Override
    public TestSpecDTO createSpec(String specName, String specJson) {
        testSpecRepository.findByName(specName)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Spec with name '" + specName + "' already exists");
                });

        TestSpecEntity entity = TestSpecEntity.builder()
                .name(specName)
                .spec(specJson)
                .createdAt(LocalDateTime.now())
                .build();

        TestSpecEntity saved = testSpecRepository.save(entity);

        return SpecMapper.mapToDTO(saved);
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
                .map(this::runTestAndSaveResult)
                .toList();
    }

    @Override
    public TestRunResultDTO runTestBySpecId(Long specId) {
        return testSpecRepository.findById(specId)
                .map(this::runTestAndSaveResult)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + specId));
    }

    @Override
    public TestRunResultDTO runTestWithSpecName(String specName) {
        return testSpecRepository.findByName(specName)
                .map(this::runTestAndSaveResult)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + specName));
    }

    @Override
    public List<TestRunResultDTO> runTestsInSpecRangeId(Long fromId, Long toId) {
        return testSpecRepository.findAllByIdBetween(fromId, toId).stream()
                .map(this::runTestAndSaveResult)
                .toList();
    }

    @Override
    public TestSpecWithRunsDTO getSpecWithRuns(Long specId) {
        return testSpecRepository.findByIdWithRuns(specId)
                .map(spec -> new TestSpecWithRunsDTO(
                        spec.getId(),
                        spec.getName(),
                        spec.getSpec(),
                        spec.getCreatedAt(),
                        spec.getRuns().stream()
                                .map(run -> new TestRunDTO(
                                        run.getId(),
                                        run.getStatus(),
                                        run.getLog(),
                                        run.getStartedAt(),
                                        run.getFinishedAt()
                                ))
                                .toList()
                ))
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + specId));
    }

    @Override
    public TestSpecDTO updateSpecById(Long specId, String specJson) {
        return testSpecRepository.findById(specId)
                .map(spec -> {
                    spec.setSpec(specJson);
                    return SpecMapper.mapToDTO(testSpecRepository.save(spec));
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + specId));
    }

    @Override
    public TestSpecDTO updateSpecByName(String specName, String specJson) {
        return testSpecRepository.findByName(specName)
                .map(spec -> {
                    spec.setSpec(specJson);
                    return SpecMapper.mapToDTO(testSpecRepository.save(spec));
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + specName));
    }

    @Override
    public TestSpecDTO deleteSpecById(Long specId, String specJson) {
        return testSpecRepository.findById(specId)
                .map(spec -> {
                    testSpecRepository.delete(spec);
                    return SpecMapper.mapToDTO(spec);
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + specId));
    }

    @Override
    public TestSpecDTO deleteSpecByName(String specName, String specJson) {
        return testSpecRepository.findByName(specName)
                .map(spec -> {
                    testSpecRepository.delete(spec);
                    return SpecMapper.mapToDTO(spec);
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + specName));
    }

    private TestRunResultDTO runTestAndSaveResult(TestSpecEntity testSpecEntity) {
        LocalDateTime startedAt = LocalDateTime.now();
        String status;
        String testLog;

        try {
            JsonNode jsonNode = objectMapper.readTree(testSpecEntity.getSpec());
            String url = jsonNode.get("url").asText();
            String method = jsonNode.get("method").asText().toUpperCase();
            int expectedStatus = jsonNode.get("expectedStatus").asInt();

            Response response = switch (method) {
                case "POST" -> RestAssured.given().body(jsonNode.get("body").toString()).post(url);
                case "PUT" -> RestAssured.given().body(jsonNode.get("body").toString()).put(url);
                case "DELETE" -> RestAssured.delete(url);
                default -> RestAssured.get(url);
            };

            StringBuilder resultLog = new StringBuilder();
            resultLog.append("URL: ").append(url).append("\n");
            resultLog.append("Method: ").append(method).append("\n");
            resultLog.append("Expected Status: ").append(expectedStatus).append("\n");
            resultLog.append("Actual Status: ").append(response.statusCode()).append("\n");

            if (response.statusCode() == expectedStatus) {
                status = "PASS";
                resultLog.append("Status PASSED\n");
            } else {
                status = "FAIL";
                resultLog.append("Status FAILED\n");
            }

            testLog = resultLog.toString();
        } catch (Exception exception) {
            status = "ERROR";
            testLog = "Test failed with error: " + exception.getMessage();
        }

        LocalDateTime finishedAt = LocalDateTime.now();

        TestRunEntity run = TestRunEntity.builder()
                .spec(testSpecEntity)
                .status(status)
                .log(testLog)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();

        TestRunEntity savedRun = testRunRepository.save(run);

        return new TestRunResultDTO(savedRun.getId(), testSpecEntity.getId(), status, testLog);
    }
}
