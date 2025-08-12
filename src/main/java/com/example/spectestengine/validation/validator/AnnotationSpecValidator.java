package com.example.spectestengine.validation.validator;

import com.example.spectestengine.exception.InvalidSpecException;
import com.example.spectestengine.utils.SpecFormatNormalizer;
import com.example.spectestengine.validation.annotation.ValidSpec;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AnnotationSpecValidator implements ConstraintValidator<ValidSpec, String> {
    @Override
    public boolean isValid(String rawSpec, ConstraintValidatorContext context) {
        try {
            /*
            Since the work and execution of the test is supposed to be in json format,
            it is necessary to normalize and validate rawSpec here
             */
            JsonNode normalizedJsonSpec = SpecFormatNormalizer.normalizeToJson(rawSpec);
            SpecValidator.validate(normalizedJsonSpec);
            return true;
        } catch (InvalidSpecException exception) {
            addConstraintViolation(context, exception.getMessage());
            return false;
        } catch (Exception exception) {
            log.error("Unexpected error during validation", exception);
            addConstraintViolation(context, "Invalid specification format");
            return false;
        }
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}