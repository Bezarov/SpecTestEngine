package com.example.spectestengine.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NotBlank(message = "Spec name cannot be blank")
@Size(min = 1, max = 255, message = "Spec name must be between 1 and 255 characters")
@Pattern(regexp = "^[a-zA-Z0-9_\\-\\s]+$", message = "Spec name can only contain letters, numbers, spaces, hyphens and underscores")
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidSpecName {
    String message() default "Invalid spec name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
