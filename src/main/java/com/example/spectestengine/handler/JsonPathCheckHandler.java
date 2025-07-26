package com.example.spectestengine.handler;

import static com.example.spectestengine.utils.Constants.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class JsonPathCheckHandler implements TestCheckHandler {

    @Override
    public String handle(JsonNode specification, Response response, ObjectNode resultLog, String handlerStatus) {
        if (specification.has(EXCEPTED_BODY_JSON_PATH) && specification.has(EXCEPTED_BODY_JSON_VALUE)) {

            String exceptedJsonPath = specification.get(EXCEPTED_BODY_JSON_PATH).asText();
            String expectedJsonValue = specification.get(EXCEPTED_BODY_JSON_VALUE).asText();
            String receivedJsonPathValue = response.jsonPath().getString(exceptedJsonPath);

            resultLog.put(EXCEPTED_BODY_JSON_PATH, exceptedJsonPath);
            resultLog.put(EXCEPTED_BODY_JSON_VALUE, expectedJsonValue);
            resultLog.put(RECEIVED_BODY_JSON_PATH_VALUE, receivedJsonPathValue);

            boolean isEqual = expectedJsonValue.equals(receivedJsonPathValue);
            resultLog.put(BODY_JSON_PATH_VALUE_CHECK_RESULT, isEqual ? PASS : FAIL);
            return isEqual ? handlerStatus : FAIL;
        }
        return handlerStatus;
    }
}
