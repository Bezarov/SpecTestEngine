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
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Spec not found with ID: " + specId));
    }

    @Override
    public TestRunResultDTO runTestWithSpecName(String specName) {
        return testSpecRepository.findByName(specName)
                .map(this::runTestAndSaveResult)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Spec not found with name: " + specName));
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
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Spec not found"));
    }

    @Override
    public TestSpecDTO updateSpecById(String specId, String specJson) {
        Long id = Long.parseLong(specId);

        return testSpecRepository.findById(id)
                .map(spec -> {
                    spec.setSpec(specJson);
                    TestSpecEntity updated = testSpecRepository.save(spec);
                    return SpecMapper.mapToDTO(spec);
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Spec not found with ID: " + id));
    }

    @Override
    public TestSpecDTO updateSpecByName(String specName, String specJson) {
        return testSpecRepository.findByName(specName)
                .map(spec -> {
                    spec.setSpec(specJson);
                    TestSpecEntity updated = testSpecRepository.save(spec);
                    return SpecMapper.mapToDTO(spec);
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Spec not found with name: " + specName));
    }

    @Override
    public TestSpecDTO deleteSpecById(String specId, String specJson) {
        Long id = Long.parseLong(specId);

        return testSpecRepository.findById(id)
                .map(spec -> {
                    testSpecRepository.delete(spec);
                    return SpecMapper.mapToDTO(spec);
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Spec not found with ID: " + id));
    }

    @Override
    public TestSpecDTO deleteSpecByName(String specName, String specJson) {
        return testSpecRepository.findByName(specName)
                .map(spec -> {
                    testSpecRepository.delete(spec);
                    return SpecMapper.mapToDTO(spec);
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Spec not found with name: " + specName));
    }

    private TestRunResultDTO runTestAndSaveResult(TestSpecEntity testSpecEntity) {
        LocalDateTime startedAt = LocalDateTime.now();
        String status;
        String log;

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

            log = resultLog.toString();
        } catch (Exception ex) {
            status = "ERROR";
            log = "Test failed with error: " + ex.getMessage();
        }

        LocalDateTime finishedAt = LocalDateTime.now();

        TestRunEntity run = TestRunEntity.builder()
                .spec(testSpecEntity)
                .status(status)
                .log(log)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();

        TestRunEntity savedRun = testRunRepository.save(run);

        return new TestRunResultDTO(savedRun.getId(), testSpecEntity.getId(), status, log);
    }
}
