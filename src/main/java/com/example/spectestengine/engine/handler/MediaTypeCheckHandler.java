package com.example.spectestengine.engine.handler;

import static com.example.spectestengine.utils.Constants.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class MediaTypeCheckHandler implements TestCheckHandler {
    @Override
    public String handle(JsonNode specification, Response response, ObjectNode resultLog, String handlerStatus) {
        if (specification.has(EXCEPTED_MEDIA_TYPE)) {

            String expectedMediaType = specification.get(EXCEPTED_MEDIA_TYPE).asText();
            String receivedMediaType = response.getContentType();

            resultLog.put(EXCEPTED_MEDIA_TYPE, expectedMediaType);
            resultLog.put(RECEIVED_MEDIA_TYPE, receivedMediaType);

            boolean isEqual = expectedMediaType.equals(receivedMediaType);
            resultLog.put(MEDIA_TYPE_CHECK_RESULT, isEqual ? PASS : FAIL);
            return isEqual ? handlerStatus : FAIL;
        }
        return handlerStatus;
    }
}
