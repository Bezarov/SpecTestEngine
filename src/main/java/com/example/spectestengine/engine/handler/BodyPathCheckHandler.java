package com.example.spectestengine.engine.handler;

import static com.example.spectestengine.utils.Constants.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class BodyPathCheckHandler implements TestCheckHandler {

    @Override
    public String handle(JsonNode specification, JsonNode normalizedResponse, Response response, ObjectNode resultLog, String handlerStatus) {
        if (specification.has(EXCEPTED_BODY_PATHS)) {
            ArrayNode expectedBodyPathCheckArray = resultLog.putArray(EXPECTED_BODY_PATH_CHECK);
            ArrayNode expectedPaths = (ArrayNode) specification.get(EXCEPTED_BODY_PATHS);

            boolean allCheckResult = true;

            for (JsonNode expectedPath : expectedPaths) {
                String expectedBodyPath = expectedPath.hasNonNull(EXPECTED_BODY_PATH) ? expectedPath.get(EXPECTED_BODY_PATH).asText() : "";

                JsonNode expectedBodyValue = expectedPath.get(EXPECTED_BODY_VALUE);
                JsonNode receivedBodyValue = normalizedResponse.at(normalizePathToJsonPointer(expectedBodyPath));

                boolean currentCheckResult = receivedBodyValue != null
                        && !receivedBodyValue.isMissingNode()
                        && expectedBodyValue.equals(receivedBodyValue);

                if (!currentCheckResult) {
                    allCheckResult = false;
                }

                ObjectNode checkLog = expectedBodyPathCheckArray.addObject();
                checkLog.put(EXPECTED_BODY_PATH, expectedBodyPath);
                checkLog.put(EXPECTED_BODY_VALUE, expectedBodyValue);
                checkLog.put(RECEIVED_BODY_VALUE, receivedBodyValue);
                checkLog.put(BODY_PATH_VALUE_CHECK_RESULT, currentCheckResult ? PASS : FAIL);
            }

            return allCheckResult ? handlerStatus : FAIL;
        }
        return handlerStatus;
    }

    private String normalizePathToJsonPointer(String expectedBodyPath) {
        if (expectedBodyPath == null || expectedBodyPath.isBlank()) {
            return "";
        }
        return "/" + expectedBodyPath.replace(".", "/");
    }
}
