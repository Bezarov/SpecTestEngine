package com.example.spectestengine.utils;

import com.example.spectestengine.exception.InvalidSpecException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.error.MarkedYAMLException;

@Slf4j
public class SpecFormatNormalizer {

    private SpecFormatNormalizer() {
        throw new IllegalStateException("Utility class");
    }

    private static final ObjectMapper jsonObjectMapper = new ObjectMapper(new JsonFactory());
    private static final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper xmlObjectMapper = new ObjectMapper(new XmlFactory());

    public static JsonNode normalizeToJson(String rawSpec) {
        return switch (SpecFormatResolver.resolve(rawSpec)) {
            case JSON -> normalizeFromJson(rawSpec);
            case YAML -> normalizeFromYaml(rawSpec);
            case XML -> normalizeFromXml(rawSpec);
        };
    }

    private static JsonNode normalizeFromJson(String rawSpec) {
        try {
            return jsonObjectMapper.readTree(rawSpec);
        } catch (JsonProcessingException jsonProcessingException) {
            String errorMessage = String.format(
                    "Failed to validate JSON spec at line:'%d', column:'%d' error: %s",
                    jsonProcessingException.getLocation().getLineNr(),
                    jsonProcessingException.getLocation().getColumnNr(),
                    jsonProcessingException.getOriginalMessage());

            log.warn(errorMessage, jsonProcessingException.getMessage());

            throw new InvalidSpecException(errorMessage);
        }
    }

    private static JsonNode normalizeFromYaml(String rawSpec) {
        try {
            return yamlObjectMapper.readTree(rawSpec);
        } catch (JsonProcessingException jsonProcessingException) {
            MarkedYAMLException markedYAMLException = (MarkedYAMLException) jsonProcessingException.getCause();
            String errorMessage = String.format(
                    "Failed to validate YAML spec at line:'%d', column:'%d', error: %s",
                    markedYAMLException.getProblemMark().getLine(),
                    markedYAMLException.getProblemMark().getColumn(),
                    markedYAMLException.getProblem());

            log.warn(errorMessage, markedYAMLException.getMessage());

            throw new InvalidSpecException(errorMessage);
        }
    }

    private static JsonNode normalizeFromXml(String rawSpec) {
        try {
            JsonNode node = xmlObjectMapper.readTree(rawSpec);
            return convertTypesInJsonNode(node);
        } catch (JsonProcessingException jsonProcessingException) {
            String errorMessage = String.format(
                    "Failed to validate XML spec at line:'%d', column:'%d' error: %s",
                    jsonProcessingException.getLocation().getLineNr(),
                    jsonProcessingException.getLocation().getColumnNr(),
                    jsonProcessingException.getOriginalMessage());

            log.warn(errorMessage, jsonProcessingException.getMessage());

            throw new InvalidSpecException(errorMessage);
        }
    }

    private static JsonNode convertTypesInJsonNode(JsonNode node) {
        return switch (node) {
            case ObjectNode objectNode -> {
                objectNode.properties().forEach(entry ->
                        entry.setValue(convertTypesInJsonNode(entry.getValue()))
                );
                yield objectNode;
            }
            case ArrayNode arrayNode -> {
                for (int i = 0; i < arrayNode.size(); i++) {
                    arrayNode.set(i, convertTypesInJsonNode(arrayNode.get(i)));
                }
                yield arrayNode;
            }
            case TextNode textNode -> {
                String text = textNode.textValue();
                yield switch (text) {
                    case "true", "TRUE", "True" -> jsonObjectMapper.getNodeFactory().booleanNode(true);
                    case "false", "FALSE", "False" -> jsonObjectMapper.getNodeFactory().booleanNode(false);
                    default -> {
                        if (text.matches("-?\\d+")) {
                            long longValue = Long.parseLong(text);
                            if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                                yield jsonObjectMapper.getNodeFactory().numberNode((int) longValue);
                            } else {
                                yield jsonObjectMapper.getNodeFactory().numberNode(longValue);
                            }
                        }
                        if (text.matches("-?\\d*\\.\\d+")) {
                            double doubleValue = Double.parseDouble(text);
                            yield jsonObjectMapper.getNodeFactory().numberNode(doubleValue);
                        }
                        yield textNode;
                    }
                };
            }
            default -> node;
        };
    }
}
