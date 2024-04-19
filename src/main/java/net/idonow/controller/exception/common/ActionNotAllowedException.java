package net.idonow.controller.exception.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ActionNotAllowedException extends RuntimeException {

    private final HttpStatus status;

    public ActionNotAllowedException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public ActionNotAllowedException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
