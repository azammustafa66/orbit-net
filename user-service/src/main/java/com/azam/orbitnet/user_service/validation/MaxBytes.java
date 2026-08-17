package com.azam.orbitnet.user_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bounds a string by its UTF-8 <em>byte</em> length rather than its character count.
 *
 * <p>{@code @Size} counts characters, so a 30-character emoji password clears
 * {@code @Size(max = 72)} while occupying 120 bytes — which BCrypt then rejects at
 * hashing time, turning a bad request into a 500. This constraint catches it during
 * validation instead.
 */
@Documented
@Constraint(validatedBy = MaxBytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxBytes {

    String message() default "must not exceed {value} bytes";

    int value();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
