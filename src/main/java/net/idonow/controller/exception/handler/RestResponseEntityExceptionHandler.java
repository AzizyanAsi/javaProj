package net.idonow.controller.exception.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.google.cloud.storage.StorageException;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import net.idonow.common.api.ApiResponseHelpers;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.exception.common.ActionNotAllowedException;
import net.idonow.controller.exception.common.BigDecimalScaleException;
import net.idonow.controller.exception.common.EntityNotFoundException;
import net.idonow.controller.exception.common.InvalidRequestDataException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static net.idonow.common.data.StringConstants.*;
import static net.idonow.entity.templates.AbstractPersistentObject.*;

@Slf4j
@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private LocaleUtils localeUtils;

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode statusCode,
                                                                  @NonNull WebRequest request) {

        String defaultCause = "JSON parse error";
        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(statusCode, defaultCause);

        // EMPTY BODY case
        if (ex.getCause() == null || ex.getCause() instanceof HttpMessageNotReadableException) {
            if (ex.getMessage() != null && ex.getMessage().matches(".*request.*body.*missing.*")) {
                body.put(CAUSE, "Request body is missing");
            }
        }

        // TYPE MISMATCH case
        else if (ex.getCause() instanceof MismatchedInputException mie) {
            body.put(CAUSE, defaultCause + ": mismatched input");

            final String className = mie.getTargetType().getSimpleName().toLowerCase();

            String fieldName = mie.getPath().get(0).getFieldName();
            String expectedType = className;

            if (Stream.of("list", "set").anyMatch(className::endsWith)) {
                expectedType = "array";
            }
            body.put(FIELDS, Map.of(fieldName, expectedType + " is expected"));

        }

        // MAPPING ERROR case
        else if (ex.getCause() instanceof JsonMappingException exception) {
            if (exception.getCause() != null && exception.getCause() instanceof BigDecimalScaleException) {
                body.put(CAUSE, exception.getCause().getMessage());
                body.put(FIELDS, Map.of(((BigDecimalScaleException) exception.getCause()).getFieldName(), localeUtils.getLocalizedMessage("validation.decimal.scale")));
            }
            else {
                body.put(CAUSE, defaultCause + ": mapping error");
            }
        }

        // SYNTAX ERROR case
        else if (ex.getCause() instanceof JsonParseException) {
            body.put(CAUSE, defaultCause + ": syntax error");
        }

        String msg = ex.getMessage() == null ? defaultCause : getFirstLineOfErrorMsg(ex.getMessage());
        log.warn(msg);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    @Override
    protected @NonNull ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                           @NonNull HttpHeaders headers,
                                                                           @NonNull HttpStatusCode statusCode,
                                                                           @NonNull WebRequest request) {
        String cause = "Request has non-valid fields";
        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(statusCode, cause);
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            String defaultMessage = fe.getDefaultMessage();
            fields.put(fe.getField(), defaultMessage != null ? defaultMessage : "");
        }
        body.put(FIELDS, fields);
        log.warn("{}: {}", cause, fields);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Exception is being thrown when validating request params or path variables
    @ExceptionHandler(value = jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(jakarta.validation.ConstraintViolationException constraintViolationException) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(HttpStatusCode.valueOf(status.value()), "Request has non-valid fields");
        Map<String, String> fields = new LinkedHashMap<>();
        for (ConstraintViolation<?> cv : constraintViolationException.getConstraintViolations()) {
            String defaultMessage = cv.getMessage();
            // Property path structure: *.functionName.fieldName
            String[] elems = cv.getPropertyPath().toString().split("\\.");
            fields.put(elems[elems.length - 1], defaultMessage != null ? defaultMessage : "");
        }
        body.put(FIELDS, fields);
        return new ResponseEntity<>(body, status);
    }

    @Override
    protected @NonNull ResponseEntity<Object> handleNoHandlerFoundException(@NonNull NoHandlerFoundException ex,
                                                                            @NonNull HttpHeaders headers,
                                                                            @NonNull HttpStatusCode statusCode,
                                                                            @NonNull WebRequest request) {

        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(statusCode, "Doesn't found any handler");
        log.error("Request error: " + getFirstLineOfErrorMsg(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException div) {

        HttpStatus status = HttpStatus.CONFLICT;
        String defaultCause = "Integrity constraint violation";
        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(HttpStatusCode.valueOf(status.value()), defaultCause);

        if (div.getCause() instanceof ConstraintViolationException cve) {

            String constraintName = cve.getConstraintName();

            // UNIQUE KEY constraint case
            if (constraintName.startsWith(PFX_UNIQUE)) {
                String cause = "Unique constraint violation";
                body.put(CAUSE, cause);

                String[] fieldNames = uniqueKeyToFieldNames(constraintName);

                String message = fieldNames.length > 1
                        ? localeUtils.getLocalizedMessage("error.entity.already-exists.combination")
                        : localeUtils.getLocalizedMessage("error.entity.already-exists");

                Map<String, String> fields = new LinkedHashMap<>();
                for (String fn : fieldNames) {
                    fields.put(fn, message);
                }
                body.put(FIELDS, fields);

                log.error(cause + ": " + fields);
            }

            // FOREIGN KEY constraint case
            else if (constraintName.startsWith(PFX_FOREIGN)) {
                String cause = "Foreign key constraint violation";
                body.put(CAUSE, cause);

                String[] fieldNames = foreignKeyToEntities(constraintName);

                String message = localeUtils.getLocalizedMessage("error.entity.used-as-reference");

                body.put(MSG, message);
                body.put(FIELDS, fieldNames);

                log.error(cause + ": " + fieldNames[0] + " -> " + fieldNames[1]);
            }

            // CHECK constraint case
            else if (constraintName.startsWith(PFX_CHECK)) {
                String cause = "Check constraint violation";
                body.put(CAUSE, cause);

                String[] fieldNames = checkConstraintToFieldNames(constraintName);
                String message = localeUtils.getLocalizedMessage("error.entity.incompatible-values");
                Map<String, String> fields = new LinkedHashMap<>();
                for (String fn : fieldNames) {
                    fields.put(fn, message);
                }
                body.put(FIELDS, fields);

                log.error(cause + ": " + fields);
            }

            // Unknown constraint violation
            else {
                String cause = "Data constraint violation";
                body.put(CAUSE, cause);
                body.put(MSG, "Unknown data error");

                log.error(cause + ": SQLState=" + cve.getSQLState());
            }

        } else if (div.getCause() instanceof DataException) {
            SQLException sqlException = ((DataException) div.getCause()).getSQLException();

            // Numeric value out of range
            if (sqlException.getSQLState().equals("22003")) {
                body.put(CAUSE, "Numeric value out of range");
                body.put(MSG, localeUtils.getLocalizedMessage("error.value.numeric-value-out-of-range"));
            } else {
                body.put(MSG, "Unknown data exception");
            }
            log.error("Data exception: SQLState={}", sqlException.getSQLState());
        } else {
            log.error("Unknown data integrity violation");
        }
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(value = EmptyResultDataAccessException.class)
    public ResponseEntity<Object> handleEmptyResultDataAccess(EmptyResultDataAccessException emptyResultDataAccessException) {

        HttpStatus status = HttpStatus.NOT_FOUND;
        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(HttpStatusCode.valueOf(status.value()), "Requested entity not found");
        log.warn(emptyResultDataAccessException.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected @NonNull ResponseEntity<Object> handleMissingServletRequestParameter(@NonNull MissingServletRequestParameterException ex,
                                                                                   @NonNull HttpHeaders headers,
                                                                                   @NonNull HttpStatusCode statusCode,
                                                                                   @NonNull WebRequest request) {
        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(statusCode, "Missing required request parameter");
        body.put(FIELDS, Map.of(ex.getParameterName(), localeUtils.getLocalizedMessage("validation.empty")));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(value = InvalidRequestDataException.class)
    public ResponseEntity<Object> handleInvalidRequestDataException(InvalidRequestDataException ex) {

        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(HttpStatusCode.valueOf(ex.getStatus().value()), ex.getMessage());
        body.put(FIELDS, ex.getFieldInfo());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(HttpStatusCode.valueOf(status.value()), ex.getMessage());
        body.put(MSG, localeUtils.getLocalizedMessage("error.entity-not-found"));
        log.warn(ex.getMessage());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(value = ActionNotAllowedException.class)
    public ResponseEntity<Object> handleActionNotAllowedException(ActionNotAllowedException ex) {
        HttpStatus status = ex.getStatus();
        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(HttpStatusCode.valueOf(status.value()), ex.getMessage());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(value = StorageException.class)
    public ResponseEntity<Object> handleStorageException(StorageException storageException) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = ApiResponseHelpers.getDefaultErrorResponseBody(status, "Application storage error");
        log.error("Storage error: {}", storageException.getMessage());
        return new ResponseEntity<>(body, status);
    }

    /* -- PRIVATE METHODS -- */

    private static String[] uniqueKeyToFieldNames(String ukName) {
        String[] keysNames;

        // Unique by primary key or custom defined 
        if (ukName.endsWith(SFX_PKEY)) {
            keysNames = ukName.substring(0, ukName.length() - SFX_PKEY.length()).split("_and_");
        } else {
            keysNames = ukName.substring(PFX_UNIQUE.length()).split("__");
        }

        for (int i = 0; i < keysNames.length; i++) {
            String key = keysNames[i];

            // Cut id suffix if exists
            if (key.endsWith(SFX_ID)) {
                key = key.substring(0, key.length() - SFX_ID.length());
            }
            keysNames[i] = underscoreToCamelcase(key);
        }
        return keysNames;
    }

    private static String[] foreignKeyToEntities(String fkName) {

        String[] keyNames = fkName.substring(PFX_FOREIGN.length()).split("__");

        // Convert to camel-case
        for (int i = 0; i < keyNames.length; i++) {
            keyNames[i] = underscoreToCamelcase(keyNames[i]);
        }

        return keyNames;
    }

    private static String[] checkConstraintToFieldNames(String ckName) {
        String[] keyNames = ckName.substring(PFX_CHECK.length()).split("__");

        // Convert to camel-case
        for (int i = 0; i < keyNames.length; i++) {
            keyNames[i] = underscoreToCamelcase(keyNames[i]);
        }

        return keyNames;
    }

    // Makes 'word_word' -> 'wordWord'
    private static String underscoreToCamelcase(String underscore) {
        String[] words = underscore.split("_");
        for (int i = 1; i < words.length; i++) {
            words[i] = StringUtils.capitalize(words[i]);
        }
        return String.join("", words);
    }

    private static String getFirstLineOfErrorMsg(String msg) {
        return msg.split(System.lineSeparator())[0];
    }

}
