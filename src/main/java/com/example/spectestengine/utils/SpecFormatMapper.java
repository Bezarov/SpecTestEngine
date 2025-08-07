package com.example.spectestengine.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public final class SpecFormatMapper {

    private SpecFormatMapper() {
        throw new IllegalStateException("Utility class");
    }

    private static final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonObjectMapper = new ObjectMapper(new JsonFactory());

    public static String toJson(Object object) {
        try {
            return jsonObjectMapper.writeValueAsString(object);
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
            return jsonObjectMapper.readTree(json);
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

    public static String toYaml(Object object) {
        try {
            return yamlObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            String errorMessage = String.format(
                    "Failed to serialize to YAML at line '%d', column '%d'",
                    exception.getLocation().getLineNr(),
                    exception.getLocation().getColumnNr()
            );
            log.warn(errorMessage, exception.getMessage());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    public static JsonNode fromYaml(String yaml) {
        try {
            return yamlObjectMapper.readTree(yaml);
        } catch (JsonProcessingException exception) {
            String errorMessage = String.format(
                    "Failed to serialize from YAML at line '%d', column '%d'",
                    exception.getLocation().getLineNr(),
                    exception.getLocation().getColumnNr()
            );
            log.warn(errorMessage, exception.getMessage());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    public static String normalizeToJson(String rawSpec) {
        try {
            Object specContent = yamlObjectMapper.readValue(rawSpec, Object.class);
            return jsonObjectMapper.writeValueAsString(specContent);
        } catch (Exception yamlEx) {
            try {
                jsonObjectMapper.readTree(rawSpec);
                return rawSpec;
            } catch (JsonProcessingException exception) {
                String errorMessage = String.format(
                        "Failed to normalize from rawSpec at line '%d', column '%d'",
                        exception.getLocation().getLineNr(),
                        exception.getLocation().getColumnNr()
                );
                log.warn(errorMessage, exception.getMessage());

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
            }
        }
    }
}
