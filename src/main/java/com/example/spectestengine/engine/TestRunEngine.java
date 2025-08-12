package com.example.spectestengine.engine;

import static com.example.spectestengine.utils.Constants.*;
import static io.restassured.config.EncoderConfig.encoderConfig;

import com.example.spectestengine.engine.handler.BodyCheckHandler;
import com.example.spectestengine.engine.handler.BodyPathCheckHandler;
import com.example.spectestengine.engine.handler.ContentTypeCheckHandler;
import com.example.spectestengine.engine.handler.StatusCodeCheckHandler;
import com.example.spectestengine.engine.handler.TestCheckHandler;
import com.example.spectestengine.model.TestRunEntity;
import com.example.spectestengine.model.TestSpecEntity;
import com.example.spectestengine.utils.SpecExtractor;
import com.example.spectestengine.utils.SpecFormatNormalizer;
import com.example.spectestengine.validation.validator.SpecValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
    private static final ObjectMapper jsonObjectMapper = new ObjectMapper();
    private final TestRunQueue testRunQueue;

    public TestRunEngine(TestRunQueue testRunQueue) {
        this.testRunQueue = testRunQueue;
    }

    private final List<TestCheckHandler> checkHandlers = List.of(
            new StatusCodeCheckHandler(), new ContentTypeCheckHandler(),
            new BodyPathCheckHandler(), new BodyCheckHandler()
    );

    public TestRunEntity buildTestRun(TestSpecEntity specEntity) {
        log.info("Building test run");
        JsonNode jsonNode = SpecFormatNormalizer.normalizeToJson(specEntity.getSpec());
        var validatedSpec = SpecValidator.validate(jsonNode);

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
        ObjectNode resultLog = jsonObjectMapper.createObjectNode();

        try {
            resultLog.put(URL, url);
            resultLog.put(METHOD, method);
            String format = specEntity.getFormat().getMediaType().toString();
            String rawSpec = specEntity.getSpec();

            Response response = buildRequestSpecification(jsonSpecNode, rawSpec, format, url, method);
            JsonNode normalizedResponse = SpecFormatNormalizer.normalizeToJson(response.getBody().asString());

            for (TestCheckHandler handler : checkHandlers) {
                overallTestStatus = handler.handle(jsonSpecNode, normalizedResponse, response, resultLog, overallTestStatus);
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
                .testResultLog(resultLog.toString())
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();
    }

    private Response buildRequestSpecification(JsonNode jsonSpecNode, String rawSpec, String format, String url, String method) throws JsonProcessingException {
        RequestSpecification requestSpecification = RestAssured.given();

        if (jsonSpecNode.has(HEADERS)) {
            jsonSpecNode.get(HEADERS).properties().forEach(header ->
                    requestSpecification.header(header.getKey(), header.getValue().asText())
            );
        }

        if (jsonSpecNode.has(BODY)) {
            String rawBody = SpecExtractor.extractRawBody(rawSpec);
            requestSpecification
                    .body(rawBody)
                    .contentType(format);

            requestSpecification.config(RestAssured.config()
                    .encoderConfig(encoderConfig().defaultContentCharset("UTF-8")
                            .encodeContentTypeAs(format, ContentType.TEXT)));
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
}
