package com.example.spectestengine.engine.handler;

import static com.example.spectestengine.utils.Constants.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class ContentTypeCheckHandler implements TestCheckHandler {
    @Override
    public String handle(JsonNode specification, JsonNode normalizedResponse, Response response, ObjectNode resultLog, String handlerStatus) {
        if (specification.has(EXCEPTED_CONTENT_TYPE)) {

            String expectedMediaType = specification.get(EXCEPTED_CONTENT_TYPE).asText();
            String receivedMediaType = response.getContentType();

            resultLog.put(EXCEPTED_CONTENT_TYPE, expectedMediaType);
            resultLog.put(RECEIVED_CONTENT_TYPE, receivedMediaType);

            boolean isEqual = expectedMediaType.equals(receivedMediaType);
            resultLog.put(CONTENT_TYPE_CHECK_RESULT, isEqual ? PASS : FAIL);
            return isEqual ? handlerStatus : FAIL;
        }
        return handlerStatus;
    }
}
