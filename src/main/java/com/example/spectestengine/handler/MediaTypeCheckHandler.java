package com.example.spectestengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class MediaTypeCheckHandler implements TestCheckHandler {
    private static final String EXCEPTED_MEDIA_TYPE = "expectedMediaType";
    private static final String ACTUAL_MEDIA_TYPE = "actualMediaType";
    private static final String PASS = "----------------PASS-------------------";
    private static final String FAIL = "----------------FAIL-------------------";

    @Override
    public String handle(JsonNode specification, Response response, ObjectNode logNode, String handlerStatus) {
        if (specification.has(EXCEPTED_MEDIA_TYPE)) {
            String expectedMediaType = specification.get(EXCEPTED_MEDIA_TYPE).asText();
            String actualMediaType = response.getContentType();

            logNode.put(EXCEPTED_MEDIA_TYPE, expectedMediaType);
            logNode.put(ACTUAL_MEDIA_TYPE, actualMediaType);

            boolean isEqual = expectedMediaType.equals(actualMediaType);
            logNode.put("mediaTypeCheckResult", isEqual ? PASS : FAIL);
            return isEqual ? handlerStatus : FAIL;
        }
        return handlerStatus;
    }
}
