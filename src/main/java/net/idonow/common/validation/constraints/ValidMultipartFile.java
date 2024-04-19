package net.idonow.common.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import net.idonow.common.validation.validators.MultipartFileValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = MultipartFileValidator.class)
public @interface ValidMultipartFile {

    String message() default "file does not match the format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

