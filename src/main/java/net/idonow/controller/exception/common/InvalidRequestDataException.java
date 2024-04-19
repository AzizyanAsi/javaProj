package net.idonow.controller.exception.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class InvalidRequestDataException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, String> fieldInfo;

    public InvalidRequestDataException(String message, Map<String, String> fieldInfo) {
        this(message, fieldInfo, HttpStatus.BAD_REQUEST);
    }

    public InvalidRequestDataException(String message, Map<String, String> fieldInfo, HttpStatus status) {
        super(message);
        this.fieldInfo = fieldInfo;
        this.status = status;
    }
}
