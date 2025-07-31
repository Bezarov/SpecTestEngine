package com.example.spectestengine.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NotNull(message = "Spec ID cannot be null")
@Positive(message = "Spec ID must be positive")
@Max(value = Long.MAX_VALUE, message = "Spec ID is too large")
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidSpecId {
    String message() default "Invalid spec ID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
