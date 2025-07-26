package com.example.spectestengine.handler;

import static com.example.spectestengine.utils.Constants.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Iterator;

@Slf4j
public class BodyCheckHandler implements TestCheckHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handle(JsonNode specification, Response response, ObjectNode resultLog, String handlerStatus) {
        if (specification.has(EXPECTED_BODY)) {
            try {
                JsonNode expectedBody = specification.get(EXPECTED_BODY).deepCopy();
                JsonNode receivedBody = objectMapper.readTree(response.getBody().asString());

                validateJsonBodies(expectedBody, receivedBody);

                applyExclusions(specification, expectedBody, receivedBody);
                applyIncludeOnlyExpectedIfNeeded(specification, expectedBody, receivedBody);

                resultLog.set(EXPECTED_BODY, expectedBody);
                resultLog.set(COMPARED_BODY, receivedBody);

                boolean isMatch = matches(expectedBody, receivedBody);
                resultLog.put(BODY_CHECK_RESULT, isMatch ? PASS : FAIL);

                resultLog.set(RECEIVED_BODY, objectMapper.readTree(response.getBody().asString()));
                return isMatch ? handlerStatus : FAIL;

            } catch (Exception exception) {
                log.warn("BodyCheckHandler error: '{}'", exception.getMessage());
                resultLog.put(BODY_CHECK_ERROR, ERROR);
                return FAIL;
            }
        }
        return handlerStatus;
    }

    private void validateJsonBodies(JsonNode expectedBody, JsonNode receivedBody) {
        if (!expectedBody.isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expected body must be a JSON object: " + expectedBody.asText());
        }

        if (receivedBody.isObject()) {
            return;
        }

        if (receivedBody.isArray()) {
            for (JsonNode bodyJsonArrayElement : receivedBody) {
                if (!bodyJsonArrayElement.isObject()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Each element in the Response body array must be a JSON object: " + receivedBody.asText());
                }
            }
            return;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Response body must be a JSON object or an array of JSON objects: " + receivedBody.asText());
    }

    private void applyExclusions(JsonNode specification, JsonNode expectedBody, JsonNode receivedBody) {
        if (specification.has(EXCLUDED_BODY_FIELDS)) {
            for (JsonNode exclusionField : specification.get(EXCLUDED_BODY_FIELDS)) {
                String exclusionFieldName = exclusionField.asText();
                ((ObjectNode) expectedBody).remove(exclusionFieldName);

                if (receivedBody.isObject()) {
                    ((ObjectNode) receivedBody).remove(exclusionFieldName);
                } else {
                    for (JsonNode receivedBodyArrayElement : receivedBody)
                        ((ObjectNode) receivedBodyArrayElement).remove(exclusionFieldName);
                }
            }
        }
    }

    private void applyIncludeOnlyExpectedIfNeeded(JsonNode specification, JsonNode expectedBody, JsonNode receivedBody) {
        if (specification.has(EXCLUDE_ALL_OTHER_BODY_FIELDS) || specification.get(EXCLUDE_ALL_OTHER_BODY_FIELDS).asBoolean()) {
            if (receivedBody.isObject()) {
                ObjectNode trimmedBodyToCompare = trimToComparableFields(expectedBody, receivedBody);
                ((ObjectNode) receivedBody).removeAll();
                ((ObjectNode) receivedBody).setAll(trimmedBodyToCompare);
                return;
            }

            for (JsonNode bodyJsonArrayElement : receivedBody) {
                ObjectNode trimmedArrayBodyElementToCompare = trimToComparableFields(expectedBody, bodyJsonArrayElement);
                ((ObjectNode) bodyJsonArrayElement).removeAll();
                ((ObjectNode) bodyJsonArrayElement).setAll(trimmedArrayBodyElementToCompare);
            }
        }
    }

    private ObjectNode trimToComparableFields(JsonNode expectedBody, JsonNode bodyToTrim) {
        ObjectNode trimmedToExpectedBody = objectMapper.createObjectNode();

        for (String expectedBodyField : getIterable(expectedBody.fieldNames())) {
            if (bodyToTrim.has(expectedBodyField)) {
                JsonNode expectedChild = expectedBody.get(expectedBodyField);
                JsonNode receivedChild = bodyToTrim.get(expectedBodyField);

                if (expectedChild.isObject() && receivedChild.isObject()) {
                    trimmedToExpectedBody.set(expectedBodyField, trimToComparableFields(expectedChild, receivedChild));
                } else {
                    trimmedToExpectedBody.set(expectedBodyField, receivedChild);
                }
            }
        }

        return trimmedToExpectedBody;
    }

    private boolean matches(JsonNode expectedBody, JsonNode actualBody) {
        if (actualBody.isObject()) {
            return expectedBody.equals(actualBody);
        }

        for (JsonNode element : actualBody) {
            if (expectedBody.equals(element)) {
                return true;
            }
        }
        return false;
    }

    private Iterable<String> getIterable(final Iterator<String> iterator) {
        return () -> iterator;
    }
}