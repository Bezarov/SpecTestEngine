package com.example.spectestengine.validation.validator;

import com.example.spectestengine.exception.InvalidSpecException;
import com.example.spectestengine.validation.annotation.ValidSpecJson;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AnnotationSpecValidator implements ConstraintValidator<ValidSpecJson, String> {
    @Override
    public boolean isValid(String jsonSpec, ConstraintValidatorContext context) {
        try {
            SpecValidator.validate(jsonSpec);
            return true;
        } catch (InvalidSpecException exception) {
            addConstraintViolation(context, exception.getMessage());
            return false;
        } catch (Exception exception) {
            log.error("Unexpected error during JSON validation", exception);
            addConstraintViolation(context, "Invalid JSON specification format");
            return false;
        }
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}