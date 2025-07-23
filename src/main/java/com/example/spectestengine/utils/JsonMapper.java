package com.example.spectestengine.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class JsonMapper {

    private JsonMapper() {
        throw new IllegalStateException("Utility class");
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception exception) {
            log.warn("Failed to serialize to JSON: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.NON_AUTHORITATIVE_INFORMATION,
                    "Failed to serialize to JSON", exception);
        }
    }

    public static JsonNode fromJson(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception exception) {
            log.warn("Failed to serialize from JSON: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.NON_AUTHORITATIVE_INFORMATION,
                    "Failed to serialize from JSON", exception);
        }
    }
}
