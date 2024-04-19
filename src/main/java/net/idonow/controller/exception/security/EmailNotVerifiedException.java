package net.idonow.controller.exception.security;

import org.springframework.security.core.AuthenticationException;

public class EmailNotVerifiedException extends AuthenticationException {

    public EmailNotVerifiedException(String msg) {
        super(msg);
    }
}
