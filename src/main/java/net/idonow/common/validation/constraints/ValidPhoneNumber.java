package net.idonow.common.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import net.idonow.common.validation.validators.PhoneNumberValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
@Documented
public @interface ValidPhoneNumber {

    String message() default "malformed phone number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
