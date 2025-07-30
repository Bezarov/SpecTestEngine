package com.example.spectestengine.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public final class JsonMapper {

    private JsonMapper() {
        throw new IllegalStateException("Utility class");
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException exception) {
            String errorMessage = String.format(
                    "Failed to serialize to JSON at line '%d', column '%d'",
                    exception.getLocation().getLineNr(),
                    exception.getLocation().getColumnNr()
            );
            log.warn(errorMessage, exception.getMessage());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    public static JsonNode fromJson(String json) {
        try {
            return mapper.readTree(json);
        } catch (JsonProcessingException exception) {
            String errorMessage = String.format(
                    "Failed to serialize from JSON at line '%d', column '%d'",
                    exception.getLocation().getLineNr(),
                    exception.getLocation().getColumnNr()
            );
            log.warn(errorMessage, exception.getMessage());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }
}
