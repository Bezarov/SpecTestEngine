package com.example.spectestengine.engine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public interface TestCheckHandler {
    String handle(JsonNode specification, Response response, ObjectNode resultLog, String handlerStatus);

}