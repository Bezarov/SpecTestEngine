package com.example.spectestengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class StatusCodeCheckHandler implements TestCheckHandler {
    private static final String EXPECTED_STATUS = "expectedStatus";
    private static final String ACTUAL_STATUS = "actualStatus";
    private static final String PASS = "----------------PASS-------------------";
    private static final String FAIL = "----------------FAIL-------------------";

    @Override
    public String handle(JsonNode specification, Response response, ObjectNode logNode, String handlerStatus) {
        if (specification.has(EXPECTED_STATUS)) {
            int expectedStatusCode = specification.get(EXPECTED_STATUS).asInt();
            int actualStatusCode = response.statusCode();

            logNode.put(EXPECTED_STATUS, expectedStatusCode);
            logNode.put(ACTUAL_STATUS, actualStatusCode);

            boolean isEqual = expectedStatusCode == actualStatusCode;
            logNode.put("statusCodeCheckResult", isEqual ? PASS : FAIL);
            return isEqual ? handlerStatus : FAIL;
        }
        return handlerStatus;
    }
}