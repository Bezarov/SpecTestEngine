package com.example.spectestengine.validation.annotation;

import com.example.spectestengine.validation.validator.AnnotationSpecValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NotBlank(message = "Specification JSON cannot be blank")
@Size(min = 10, max = 10000, message = "Specification JSON must be between 10 and 10000 characters")
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AnnotationSpecValidator.class)
@Documented
public @interface ValidSpecJson {
    String message() default "Invalid specification JSON";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}