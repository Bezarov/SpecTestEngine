package com.example.spectestengine.validation.validator;

import static com.example.spectestengine.utils.Constants.*;

import com.example.spectestengine.exception.InvalidSpecException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public class SpecValidator {

    private SpecValidator() {
        throw new IllegalStateException("Validator class - cannot be instantiated");
    }

    public record ValidatedSpec(JsonNode jsonSpecNode, String url, String method) {
    }

    public static ValidatedSpec validate(JsonNode jsonSpec) {
        return getValidationChain(jsonSpec)
                .validateSpecNotEmpty(jsonSpec.toString())
                .validateJsonSpecNode()
                .validateMandatoryFields()
                .validateHttpMethod()
                .validateUrl()
                .validateHeaders()
                .validateExpectedStatusCode()
                .validateBody()
                .validateExcludedBodyFields()
                .build();
    }

    private static ValidationChain getValidationChain(JsonNode jsonSpecNode) {
        return ValidationChain.builder()
                .jsonSpecNode(jsonSpecNode)
                .build();
    }

    @Builder
    private static class ValidationChain {
        private final JsonNode jsonSpecNode;
        private String url;
        private String method;

        protected ValidationChain validateSpecNotEmpty(String jsonSpec) {
            if (jsonSpec == null || jsonSpec.trim().isEmpty() || "null".equalsIgnoreCase(jsonSpec.trim())) {
                log.error("Spec json is null or empty: {}", jsonSpec);
                throw new InvalidSpecException("Specification must not be null or empty");
            }
            return this;
        }

        protected ValidationChain validateJsonSpecNode() {
            if (!jsonSpecNode.isObject()) {
                log.error("JSON specification is not an object: '{}'", jsonSpecNode);
                throw new InvalidSpecException("Specification root must be an object");
            }
            return this;
        }

        protected ValidationChain validateMandatoryFields() {
            for (String mandatoryField : MANDATORY_FIELDS) {
                if (!jsonSpecNode.has(mandatoryField) ||
                        jsonSpecNode.get(mandatoryField).isNull() ||
                        jsonSpecNode.get(mandatoryField).asText().isBlank()) {

                    log.error("Mandatory field: '{}' is missing", mandatoryField);
                    throw new InvalidSpecException("Missing or empty mandatory field: '%s', received: '%s'"
                            .formatted(mandatoryField, jsonSpecNode.get(mandatoryField)));
                }
            }
            return this;
        }

        protected ValidationChain validateHttpMethod() {
            this.method = jsonSpecNode.get(METHOD).asText().toUpperCase();
            if (!VALID_HTTP_METHODS.contains(method)) {
                log.error("Method is not valid: '{}'", method);
                throw new InvalidSpecException("Unsupported HTTP method: '%s'".formatted(method));
            }
            return this;
        }

        protected ValidationChain validateUrl() {
            this.url = jsonSpecNode.get(URL).asText();
            if (!isUrlValid(url)) {
                log.error("URL is not valid: '{}'", url);
                throw new InvalidSpecException("Invalid URL: '%s'".formatted(url));
            }
            return this;
        }

        protected static boolean isUrlValid(String url) {
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

        protected ValidationChain validateHeaders() {
            if (!jsonSpecNode.has(HEADERS)) {
                return this;
            }

            if (!jsonSpecNode.get(HEADERS).isObject()) {
                log.error("JSON specification headers field is not an object: '{}'", jsonSpecNode.get(HEADERS));
                throw new InvalidSpecException("Field 'headers' must be a key-value object");
            }

            jsonSpecNode.get(HEADERS).properties().forEach(header -> {
                if (!header.getValue().isTextual()) {
                    log.error("Header value for: '{}' not string", header.getKey());
                    throw new InvalidSpecException("Header value for: '%s' must be a string".formatted(header.getKey()));
                }
            });

            return this;
        }

        protected ValidationChain validateExpectedStatusCode() {
            if (jsonSpecNode.has(EXPECTED_STATUS_CODE) && !jsonSpecNode.get(EXPECTED_STATUS_CODE).isNumber()) {
                log.error("JSON specification expected status code is not a number: '{}'", jsonSpecNode.get(EXPECTED_STATUS_CODE).asText());
                throw new InvalidSpecException("Field 'expectedStatusCode' must be a number");
            }
            return this;
        }

        protected ValidationChain validateBody() {
            if (jsonSpecNode.has(BODY) && !jsonSpecNode.get(BODY).isContainerNode()) {
                log.error("JSON specification body is not object or array: '{}'", jsonSpecNode.get(BODY));
                throw new InvalidSpecException("Field 'body' must be an object or array");
            }
            return this;
        }

        protected ValidationChain validateExcludedBodyFields() {
            if (jsonSpecNode.has(EXCLUDED_BODY_FIELDS) && !jsonSpecNode.get(EXCLUDED_BODY_FIELDS).isArray()) {
                log.error("Field 'excludedBodyFields' must be an array: '{}'", jsonSpecNode.get(EXCLUDED_BODY_FIELDS));
                throw new InvalidSpecException("Field 'excludedBodyFields' must be an array");
            }
            return this;
        }

        protected ValidatedSpec build() {
            return new ValidatedSpec(jsonSpecNode, url, method);
        }
    }
}