package com.example.spectestengine.validation.validator;

import static com.example.spectestengine.utils.Constants.*;

import com.example.spectestengine.exception.InvalidSpecException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public class SpecValidator {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SpecValidator() {
        throw new IllegalStateException("Validator class - cannot be instantiated");
    }

    public record ValidatedSpec(JsonNode jsonSpecNode, String url, String method) {
    }

    public static ValidatedSpec validate(String jsonSpec) {
        if (jsonSpec == null || jsonSpec.trim().isEmpty() || "null".equalsIgnoreCase(jsonSpec.trim())) {
            log.error("Spec json is null or empty: {}", jsonSpec);
            throw new InvalidSpecException("JSON specification must not be null or empty");
        }

        try {
            JsonNode jsonSpecNode = MAPPER.readTree(jsonSpec);

            if (!jsonSpecNode.isObject()) {
                log.error("JSON specification is not an object: '{}'", jsonSpec);
                throw new InvalidSpecException("JSON root must be an object");
            }

            for (String mandatoryField : MANDATORY_FIELDS) {
                if (!jsonSpecNode.has(mandatoryField) || jsonSpecNode.get(mandatoryField).isNull() || jsonSpecNode.get(mandatoryField).asText().isBlank()) {
                    log.error("Mandatory field: '{}' is missing", mandatoryField);
                    throw new InvalidSpecException("Missing or empty mandatory field: '%s', received: '%s'"
                            .formatted(mandatoryField, jsonSpecNode.get(mandatoryField)));
                }
            }

            String method = jsonSpecNode.get("method").asText().toUpperCase();
            if (!VALID_HTTP_METHODS.contains(method)) {
                log.error("Method is not valid: '{}'", method);
                throw new InvalidSpecException("Unsupported HTTP method: '%s'".formatted(method));
            }

            String url = jsonSpecNode.get("url").asText();
            if (!isUrlValid(url)) {
                log.error("URL is not valid: '{}'", url);
                throw new InvalidSpecException("Invalid URL: '%s'".formatted(url));
            }

            if (jsonSpecNode.has("headers") && !jsonSpecNode.get("headers").isObject()) {
                log.error("JSON specification headers field is not an object: '{}'", jsonSpecNode.get("headers"));
                throw new InvalidSpecException("Field 'headers' must be a JSON object");
            }

            if (jsonSpecNode.has("headers")) {
                jsonSpecNode.get("headers").properties().forEach(header -> {
                    if (!header.getValue().isTextual()) {
                        log.error("Header value for: '{}' not string", header.getKey());
                        throw new InvalidSpecException("Header value for: '%s' must be a string".formatted(header.getKey()));
                    }
                });
            }

            if (jsonSpecNode.has("expectedStatusCode") && !jsonSpecNode.get("expectedStatusCode").isNumber()) {
                log.error("JSON specification expected status code is not a number: '{}'", jsonSpecNode.get("expectedStatusCode").asText());
                throw new InvalidSpecException("Field 'expectedStatusCode' must be a number");
            }

            if (jsonSpecNode.has("body") && !jsonSpecNode.get("body").isContainerNode()) {
                log.error("JSON specification body is not object or array: '{}'", jsonSpecNode.get("body"));
                throw new InvalidSpecException("Field 'body' must be an object or array");
            }

            if (jsonSpecNode.has("excludedBodyFields") && !jsonSpecNode.get("excludedBodyFields").isArray()) {
                log.error("Field 'excludedBodyFields' must be an array: '{}'", jsonSpecNode.get("excludedBodyFields"));
                throw new InvalidSpecException("Field 'excludedBodyFields' must be an array");
            }

            return new ValidatedSpec(jsonSpecNode, url, method);

        } catch (JsonProcessingException exception) {
            String errorMessage = String.format(
                    "Failed to parse JSON at line '%d', column '%d'",
                    exception.getLocation().getLineNr(),
                    exception.getLocation().getColumnNr()
            );
            log.warn(errorMessage, exception.getMessage());
            throw new InvalidSpecException(errorMessage);
        }
    }

    private static boolean isUrlValid(String url) {
        try {
            URI uri = new URI(url);
            if (uri.isAbsolute()) {
                return true;
            }
            log.error("URL is not has absolute path: '{}'", url);
            throw new InvalidSpecException("URL must be absolute: '%s'".formatted(uri));
        } catch (Exception exception) {
            return false;
        }
    }
}
