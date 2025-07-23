package com.example.spectestengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class JsonPathCheckHandler implements TestCheckHandler {
    private static final String EXCEPTED_JSON_BODY_PATH = "expectedBodyPath";
    private static final String EXCEPTED_JSON_BODY_VALUE = "expectedBodyValue";
    private static final String ACTUAL_JSON_BODY_PATH_VALUE = "actualBodyPathValue";
    private static final String PASS = "----------------PASS-------------------";
    private static final String FAIL = "----------------FAIL-------------------";

    @Override
    public String handle(JsonNode specification, Response response, ObjectNode logNode, String handlerStatus) {
        if (specification.has(EXCEPTED_JSON_BODY_PATH) && specification.has(EXCEPTED_JSON_BODY_VALUE)) {
            String exceptedJsonPath = specification.get(EXCEPTED_JSON_BODY_PATH).asText();
            String expectedJsonValue = specification.get(EXCEPTED_JSON_BODY_VALUE).asText();
            String actualJsonValue = response.jsonPath().getString(exceptedJsonPath);

            logNode.put(EXCEPTED_JSON_BODY_PATH, exceptedJsonPath);
            logNode.put(EXCEPTED_JSON_BODY_VALUE, expectedJsonValue);
            logNode.put(ACTUAL_JSON_BODY_PATH_VALUE, actualJsonValue);

            boolean isEqual = expectedJsonValue.equals(actualJsonValue);
            logNode.put("jsonPathCheckResult", isEqual ? PASS : FAIL);
            return isEqual ? handlerStatus : FAIL;
        }
        return handlerStatus;
    }
}
