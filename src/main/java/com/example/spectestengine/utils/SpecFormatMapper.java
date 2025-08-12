package com.example.spectestengine.utils;

import com.example.spectestengine.exception.InvalidSpecException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SpecFormatMapper {

    private SpecFormatMapper() {
        throw new IllegalStateException("Utility class");
    }

    private static final ObjectMapper jsonObjectMapper = new ObjectMapper(new JsonFactory());
    private static final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper xmlObjectMapper = new ObjectMapper(new XmlFactory());

    public static String toJson(Object object) {
        try {
            return jsonObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException jsonProcessingException) {
            String errorMessage = String.format(
                    "Failed to serialize to JSON at line '%d', column '%d' error: %s",
                    jsonProcessingException.getLocation().getLineNr(),
                    jsonProcessingException.getLocation().getColumnNr(),
                    jsonProcessingException.getOriginalMessage());

            log.warn(errorMessage, jsonProcessingException.getMessage());

            throw new InvalidSpecException(errorMessage);
        }
    }

    public static JsonNode fromJson(String json) {
        try {
            return jsonObjectMapper.readTree(json);
        } catch (JsonProcessingException jsonProcessingException) {
            String errorMessage = String.format(
                    "Failed to serialize from JSON at line '%d', column '%d' error: %s",
                    jsonProcessingException.getLocation().getLineNr(),
                    jsonProcessingException.getLocation().getColumnNr(),
                    jsonProcessingException.getOriginalMessage());

            log.warn(errorMessage, jsonProcessingException.getMessage());

            throw new InvalidSpecException(errorMessage);
        }
    }

    public static String toYaml(Object object) {
        try {
            return yamlObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException jsonProcessingException) {
            String errorMessage = String.format(
                    "Failed to serialize to YAML at line '%d', column '%d' error: %s",
                    jsonProcessingException.getLocation().getLineNr(),
                    jsonProcessingException.getLocation().getColumnNr(),
                    jsonProcessingException.getOriginalMessage());

            log.warn(errorMessage, jsonProcessingException.getMessage());

            throw new InvalidSpecException(errorMessage);
        }
    }

    public static JsonNode fromYaml(String yaml) {
        try {
            return yamlObjectMapper.readTree(yaml);
        } catch (JsonProcessingException jsonProcessingException) {
            String errorMessage = String.format(
                    "Failed to serialize from YAML at line '%d', column '%d' error: %s",
                    jsonProcessingException.getLocation().getLineNr(),
                    jsonProcessingException.getLocation().getColumnNr(),
                    jsonProcessingException.getOriginalMessage());

            log.warn(errorMessage, jsonProcessingException.getMessage());

            throw new InvalidSpecException(errorMessage);
        }
    }

    public static String toXml(Object object) {
        try {
            return xmlObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException jsonProcessingException) {
            String errorMessage = String.format(
                    "Failed to serialize to XML at line '%d', column '%d' error: %s",
                    jsonProcessingException.getLocation().getLineNr(),
                    jsonProcessingException.getLocation().getColumnNr(),
                    jsonProcessingException.getOriginalMessage());

            log.warn(errorMessage, jsonProcessingException.getMessage());

            throw new InvalidSpecException(errorMessage);
        }
    }

    public static JsonNode fromXml(String xml) {
        try {
            return xmlObjectMapper.readTree(xml);
        } catch (JsonProcessingException jsonProcessingException) {
            String errorMessage = String.format(
                    "Failed to serialize from XML at line '%d', column '%d' error: %s",
                    jsonProcessingException.getLocation().getLineNr(),
                    jsonProcessingException.getLocation().getColumnNr(),
                    jsonProcessingException.getOriginalMessage());

            log.warn(errorMessage, jsonProcessingException.getMessage());

            throw new InvalidSpecException(errorMessage);
        }
    }
}