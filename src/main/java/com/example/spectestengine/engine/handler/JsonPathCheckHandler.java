package com.example.spectestengine.engine.handler;

import static com.example.spectestengine.utils.Constants.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class JsonPathCheckHandler implements TestCheckHandler {

    @Override
    public String handle(JsonNode specification, Response response, ObjectNode resultLog, String handlerStatus) {
        if (specification.has(EXCEPTED_BODY_JSON_PATHS)) {
            ArrayNode jsonPathCheckArray = resultLog.putArray(EXPECTED_JSON_PATH_CHECK);
            ArrayNode expectedPaths = (ArrayNode) specification.get(EXCEPTED_BODY_JSON_PATHS);

            boolean allCheckResult = true;

            for (JsonNode expectedPath : expectedPaths) {
                String expectedJsonPath = expectedPath.get(EXPECTED_JSON_PATH).asText();
                String expectedJsonValue = expectedPath.get(EXPECTED_JSON_VALUE).asText();
                String receivedJsonValue = response.jsonPath().getString(expectedJsonPath);

                boolean currentCheckResult = expectedJsonValue.equals(receivedJsonValue);
                if (!currentCheckResult) {
                    allCheckResult = false;
                }

                ObjectNode checkLog = jsonPathCheckArray.addObject();
                checkLog.put(EXPECTED_JSON_PATH, expectedJsonPath);
                checkLog.put(EXPECTED_JSON_VALUE, expectedJsonValue);
                checkLog.put(RECEIVED_JSON_VALUE, receivedJsonValue);
                checkLog.put(BODY_JSON_PATH_VALUE_CHECK_RESULT, currentCheckResult ? PASS : FAIL);
            }

            return allCheckResult ? handlerStatus : FAIL;
        }
        return handlerStatus;
    }
}
