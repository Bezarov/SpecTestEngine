package com.example.spectestengine.engine;

import static com.example.spectestengine.utils.Constants.*;

import com.example.spectestengine.engine.handler.BodyCheckHandler;
import com.example.spectestengine.engine.handler.JsonPathCheckHandler;
import com.example.spectestengine.engine.handler.MediaTypeCheckHandler;
import com.example.spectestengine.engine.handler.StatusCodeCheckHandler;
import com.example.spectestengine.engine.handler.TestCheckHandler;
import com.example.spectestengine.model.TestRunEntity;
import com.example.spectestengine.model.TestSpecEntity;
import com.example.spectestengine.validation.validator.SpecValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class TestRunEngine {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestRunQueue testRunQueue;

    public TestRunEngine(TestRunQueue testRunQueue) {
        this.testRunQueue = testRunQueue;
    }

    private final List<TestCheckHandler> checkHandlers = List.of(
            new StatusCodeCheckHandler(), new MediaTypeCheckHandler(),
            new JsonPathCheckHandler(), new BodyCheckHandler()
    );

    public TestRunEntity buildTestRun(TestSpecEntity specEntity) {
        log.info("Building test run");
        var validatedSpec = SpecValidator.validate(specEntity.getSpec());
        CompletableFuture<TestRunEntity> future = new CompletableFuture<>();

        testRunQueue.submit(validatedSpec.url(), () -> {
            TestRunEntity testRunEntity = executeRun(specEntity, validatedSpec.jsonSpecNode(), validatedSpec.url(), validatedSpec.method());
            future.complete(testRunEntity);
        });

        return waitForResult(future, specEntity.getId());
    }

    private TestRunEntity executeRun(TestSpecEntity specEntity, JsonNode jsonSpecNode, String url, String method) {
        LocalDateTime startedAt = LocalDateTime.now();
        String overallTestStatus = PASS;
        ObjectNode resultLog = objectMapper.createObjectNode();

        try {
            resultLog.put(URL, url);
            resultLog.put(METHOD, method);

            Response response = buildRequestSpecification(jsonSpecNode, url, method);

            for (TestCheckHandler handler : checkHandlers) {
                overallTestStatus = handler.handle(jsonSpecNode, response, resultLog, overallTestStatus);
            }

        } catch (Exception exception) {
            log.warn("Exception occurred while executing test run", exception);
            overallTestStatus = ERROR;
            resultLog.put("resultError", TEST_RUN_ERROR);
        }

        LocalDateTime finishedAt = LocalDateTime.now();

        return TestRunEntity.builder()
                .spec(specEntity)
                .status(overallTestStatus)
                .log(resultLog.toString())
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();
    }

    private TestRunEntity waitForResult(CompletableFuture<TestRunEntity> future, Long specId) {
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException timeoutException) {
            log.info("Timed out waiting for result, test still in a Queue");
            throw new ResponseStatusException(HttpStatus.ACCEPTED, (
                    "Your test task in a queue with spec id: '%s', please check test result later").formatted(specId));
        } catch (ExecutionException | InterruptedException exception) {
            log.warn("Exception while waiting for test result thread is interrupted exception: '{}'", exception.getMessage());
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Test run failed");
        }
    }

    private Response buildRequestSpecification(JsonNode specification, String url, String method) {
        RequestSpecification requestSpecification = RestAssured.given();

        if (specification.has(HEADERS)) {
            specification.get(HEADERS).properties().forEach(header ->
                    requestSpecification.header(header.getKey(), header.getValue().asText())
            );
        }

        if (specification.has(BODY)) {
            requestSpecification.body(specification.get(BODY).toString());
            requestSpecification.header("Content-Type", "application/json");
        }

        return executeHttpRequest(requestSpecification, url, method);
    }

    private Response executeHttpRequest(RequestSpecification requestSpecification, String url, String method) {
        return switch (method) {
            case "POST" -> requestSpecification.post(url);
            case "PUT" -> requestSpecification.put(url);
            case "PATCH" -> requestSpecification.patch(url);
            case "DELETE" -> requestSpecification.delete(url);
            default -> requestSpecification.get(url);
        };
    }
}
