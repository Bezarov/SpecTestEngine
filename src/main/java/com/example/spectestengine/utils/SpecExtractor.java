package com.example.spectestengine.utils;

import com.example.spectestengine.model.SpecFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import static com.example.spectestengine.utils.Constants.BODY;

public class SpecExtractor {

    private SpecExtractor() {
        throw new IllegalStateException("Utility class");
    }

    private static final ObjectMapper jsonObjectMapper = new ObjectMapper(new JsonFactory());
    private static final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    private static final XmlMapper xmlObjectMapper = new XmlMapper();

    public static String extractRawBody(String rawSpec) throws JsonProcessingException {
        SpecFormat format = SpecFormatResolver.resolve(rawSpec);

        JsonNode bodyNode = switch (format) {
            case JSON -> jsonObjectMapper.readTree(rawSpec).get(BODY);
            case YAML -> yamlObjectMapper.readTree(rawSpec).get(BODY);
            case XML -> xmlObjectMapper.readTree(rawSpec).get(BODY);
        };

        return switch (format) {
            case JSON -> jsonObjectMapper.writeValueAsString(bodyNode);
            case YAML -> yamlObjectMapper.writeValueAsString(bodyNode);
            case XML -> xmlObjectMapper.writeValueAsString(bodyNode);
        };
    }
}
