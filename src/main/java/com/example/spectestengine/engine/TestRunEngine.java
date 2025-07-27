package com.example.spectestengine.engine;

import static com.example.spectestengine.utils.Constants.*;

import com.example.spectestengine.engine.handler.BodyCheckHandler;
import com.example.spectestengine.engine.handler.JsonPathCheckHandler;
import com.example.spectestengine.engine.handler.MediaTypeCheckHandler;
import com.example.spectestengine.engine.handler.StatusCodeCheckHandler;
import com.example.spectestengine.engine.handler.TestCheckHandler;
import com.example.spectestengine.model.TestRunEntity;
import com.example.spectestengine.model.TestSpecEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        CompletableFuture<TestRunEntity> future = new CompletableFuture<>();

        testRunQueue.submit(getQueueKey(specEntity), () -> {
            TestRunEntity testRunEntity = executeRun(specEntity);
            future.complete(testRunEntity);
        });

        return waitForResult(future);
    }

    private TestRunEntity executeRun(TestSpecEntity specEntity) {
        LocalDateTime startedAt = LocalDateTime.now();
        String overallTestStatus = PASS;
        ObjectNode resultLog = objectMapper.createObjectNode();

        try {
            JsonNode specification = objectMapper.readTree(specEntity.getSpec());
            String url = specification.get(URL).asText();
            String method = specification.get(METHOD).asText().toUpperCase();

            resultLog.put(URL, url);
            resultLog.put(METHOD, method);

            Response response = buildRequestSpecification(specification, url, method);

            for (TestCheckHandler handler : checkHandlers) {
                overallTestStatus = handler.handle(specification, response, resultLog, overallTestStatus);
            }

        } catch (Exception exception) {
            overallTestStatus = ERROR;
            resultLog.put("error", REST_RUN_ERROR);
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

    private TestRunEntity waitForResult(CompletableFuture<TestRunEntity> future) {
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException timeoutException) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "Test in a queue. Please check results later.");
        } catch (ExecutionException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Test run failed");
        }
    }

    private String getQueueKey(TestSpecEntity specEntity) {
        try {
            JsonNode specification = objectMapper.readTree(specEntity.getSpec());
            return specification.get(URL).asText();
        } catch (Exception exception) {
            return "default";
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
