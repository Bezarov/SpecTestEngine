package com.example.spectestengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BodyCheckHandler implements TestCheckHandler {
    private static final String EXPECTED_BODY = "expectedBody";
    private static final String ACTUAL_BODY = "actualBody";
    private static final String EXCLUDED_FIELDS = "excludedFields";
    private static final String EXCLUDE_ALL_OTHERS = "excludeAllOtherBodyFields";
    private static final String PASS = "----------------PASS-------------------";
    private static final String FAIL = "----------------FAIL-------------------";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handle(JsonNode specification, Response response, ObjectNode logNode, String handlerStatus) {
        if (specification.has(EXPECTED_BODY)) {
            try {
                JsonNode expectedBody = specification.get(EXPECTED_BODY).deepCopy();
                JsonNode actualBody = objectMapper.readTree(response.getBody().asString());

                applyExclusions(specification, expectedBody, actualBody);
                applyIncludeOnlyExpectedIfNeeded(specification, expectedBody, actualBody);

                logNode.set(EXPECTED_BODY, expectedBody);
                logNode.set(ACTUAL_BODY, actualBody);

                boolean isMatch = matches(expectedBody, actualBody);
                logNode.put("bodyCheckResult", isMatch ? PASS : FAIL);
                return isMatch ? handlerStatus : FAIL;
            } catch (Exception exception) {
                log.warn("bodyCheckError: {}", exception.getMessage());
                logNode.put("bodyCheckError", exception.getMessage());
                return FAIL;
            }
        }
        return handlerStatus;
    }

    private void applyExclusions(JsonNode specification, JsonNode expectedBody, JsonNode actualBody) {
        if (specification.has(EXCLUDED_FIELDS)) {
            for (JsonNode field : specification.get(EXCLUDED_FIELDS)) {
                if (expectedBody.isObject())
                    ((ObjectNode) expectedBody).remove(field.asText());
                if (actualBody.isObject())
                    ((ObjectNode) actualBody).remove(field.asText());
            }
        }
    }

    private void applyIncludeOnlyExpectedIfNeeded(JsonNode specification, JsonNode expectedBody, JsonNode actualBody) {
        if (specification.has(EXCLUDE_ALL_OTHERS) && specification.get(EXCLUDE_ALL_OTHERS).asBoolean()) {
            if (actualBody.isArray() && expectedBody.isObject()) {
                for (JsonNode element : actualBody) {
                    ObjectNode trimmedJson = trimToExpectedFields(expectedBody, actualBody);
                    ((ObjectNode) element).removeAll();
                    ((ObjectNode) element).setAll(trimmedJson);
                }
            } else if (actualBody.isObject() && expectedBody.isObject()) {
                ObjectNode trimmedJson = trimToExpectedFields(expectedBody, actualBody);
                ((ObjectNode) actualBody).removeAll();
                ((ObjectNode) actualBody).setAll(trimmedJson);
            }
        }
    }

    private ObjectNode trimToExpectedFields(JsonNode expectedBody, JsonNode target) {
        ObjectNode result = objectMapper.createObjectNode();
        if (!expectedBody.isObject() || !target.isObject())
            return result;

        expectedBody.fieldNames().forEachRemaining(field -> {
            if (target.has(field)) {
                JsonNode expectedChild = expectedBody.get(field);
                JsonNode targetChild = target.get(field);

                if (expectedChild.isObject() && targetChild.isObject())
                    result.set(field, trimToExpectedFields(expectedChild, targetChild));
                else
                    result.set(field, targetChild);
            }
        });

        return result;
    }

    private boolean matches(JsonNode expectedBody, JsonNode actualBody) {
        if (actualBody.isArray() && expectedBody.isObject()) {
            for (JsonNode element : actualBody) {
                if (expectedBody.equals(element))
                    return true;
            }
            return false;
        } else {
            return expectedBody.equals(actualBody);
        }
    }
}