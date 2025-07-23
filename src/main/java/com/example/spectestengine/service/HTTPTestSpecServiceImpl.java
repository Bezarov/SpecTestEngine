package com.example.spectestengine.service;

import com.example.spectestengine.dto.TestRunDTO;
import com.example.spectestengine.dto.TestRunResultDTO;
import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.dto.TestSpecWithRunsDTO;
import com.example.spectestengine.handler.BodyCheckHandler;
import com.example.spectestengine.handler.JsonPathCheckHandler;
import com.example.spectestengine.handler.MediaTypeCheckHandler;
import com.example.spectestengine.handler.StatusCodeCheckHandler;
import com.example.spectestengine.handler.TestCheckHandler;
import com.example.spectestengine.model.TestRunEntity;
import com.example.spectestengine.model.TestSpecEntity;
import com.example.spectestengine.repository.TestRunRepository;
import com.example.spectestengine.repository.TestSpecRepository;
import com.example.spectestengine.utils.JsonMapper;
import com.example.spectestengine.utils.SpecMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class HTTPTestSpecServiceImpl implements HTTPTestSpecService {
    private static final String SPEC_NOT_FOUND_LOG = "Specification not found with ";
    private final TestSpecRepository testSpecRepository;
    private final TestRunRepository testRunRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<TestCheckHandler> checkHandlers = List.of(
            new StatusCodeCheckHandler(), new MediaTypeCheckHandler(),
            new BodyCheckHandler(), new JsonPathCheckHandler()
    );

    public HTTPTestSpecServiceImpl(TestSpecRepository testSpecRepository, TestRunRepository testRunRepository) {
        this.testSpecRepository = testSpecRepository;
        this.testRunRepository = testRunRepository;
    }

    @Override
    public TestSpecDTO createSpec(String specName, String specJson) {
        testSpecRepository.findByName(specName)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Specification with name '" + specName + "' already exists");
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
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + "id: " + specId));
    }

    @Override
    public TestRunResultDTO runTestWithSpecName(String specName) {
        return testSpecRepository.findByName(specName)
                .map(this::runTestAndSaveResult)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, SPEC_NOT_FOUND_LOG + "name: " + specName));
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

    private TestRunResultDTO runTestAndSaveResult(TestSpecEntity specEntity) {
        LocalDateTime startedAt = LocalDateTime.now();
        String overalTestStatus = "----------------PASS-------------------";
        ObjectNode logNode = objectMapper.createObjectNode();

        try {
            JsonNode specification = objectMapper.readTree(specEntity.getSpec());
            String url = specification.get("url").asText();
            String method = specification.get("method").asText().toUpperCase();
            logNode.put("url", url);
            logNode.put("method", method);

            Response response = executeHttpRequest(specification, url, method);

            for (TestCheckHandler handler : checkHandlers)
                overalTestStatus = handler.handle(specification, response, logNode, overalTestStatus);

        } catch (Exception exception) {
            overalTestStatus = "----------------ERROR-------------------";
            logNode.put("error", exception.getMessage());
        }

        LocalDateTime finishedAt = LocalDateTime.now();

        TestRunEntity run = TestRunEntity.builder()
                .spec(specEntity)
                .status(overalTestStatus)
                .log(logNode.toString())
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();

        TestRunEntity saved = testRunRepository.save(run);

        return new TestRunResultDTO(
                saved.getId(),
                specEntity.getId(),
                overalTestStatus,
                JsonMapper.fromJson(logNode.toString()),
                run.getStartedAt().truncatedTo(ChronoUnit.SECONDS),
                run.getFinishedAt().truncatedTo(ChronoUnit.SECONDS)
        );
    }

    private Response executeHttpRequest(JsonNode spec, String url, String method) {
        return switch (method) {
            case "POST" -> RestAssured.given().body(spec.get("body").toString()).post(url);
            case "PUT" -> RestAssured.given().body(spec.get("body").toString()).put(url);
            case "DELETE" -> RestAssured.delete(url);
            default -> RestAssured.get(url);
        };
    }
}