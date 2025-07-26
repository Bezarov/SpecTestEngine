package com.example.spectestengine.handler;

import static com.example.spectestengine.utils.Constants.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class StatusCodeCheckHandler implements TestCheckHandler {
    @Override
    public String handle(JsonNode specification, Response response, ObjectNode resultLog, String handlerStatus) {
        if (specification.has(EXPECTED_STATUS_CODE)) {
            int expectedStatusCode = specification.get(EXPECTED_STATUS_CODE).asInt();
            int receivedStatusCode = response.statusCode();

            resultLog.put(EXPECTED_STATUS_CODE, expectedStatusCode);
            resultLog.put(RECEIVED_STATUS_CODE, receivedStatusCode);

            boolean isEqual = expectedStatusCode == receivedStatusCode;
            resultLog.put(STATUS_CODE_CHECK_RESULT, isEqual ? PASS : FAIL);
            return isEqual ? handlerStatus : FAIL;
        }
        return handlerStatus;
    }
}