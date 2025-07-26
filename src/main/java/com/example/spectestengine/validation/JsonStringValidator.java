package com.example.spectestengine.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class JsonStringValidator {
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonStringValidator() {
        throw new IllegalStateException("Validator class");
    }

    public static void validate(String inputJson) {
        try {
            JsonNode jsonNode = mapper.readTree(inputJson);

            if (inputJson.equalsIgnoreCase("null"))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON specification cannot be null");
            if (jsonNode.isObject() && jsonNode.size() == 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON object specification cannot be empty");
            if (jsonNode.isArray() && jsonNode.size() == 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON array specification cannot be empty");

        } catch (JsonProcessingException exception) {
            String errorMessage = String.format(
                    "Failed to parse JSON at line %d, column %d ",
                    exception.getLocation().getLineNr(),
                    exception.getLocation().getColumnNr()
            );
            log.warn(errorMessage, exception.getMessage());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }
}
