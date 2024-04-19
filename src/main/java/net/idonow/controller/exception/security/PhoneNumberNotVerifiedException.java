package net.idonow.controller.exception.security;

import org.springframework.security.core.AuthenticationException;

public class PhoneNumberNotVerifiedException extends AuthenticationException {

    public PhoneNumberNotVerifiedException(String msg) {
        super(msg);
    }
}
