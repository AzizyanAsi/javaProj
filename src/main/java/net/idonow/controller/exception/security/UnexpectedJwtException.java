package net.idonow.controller.exception.security;

import io.jsonwebtoken.JwtException;

public class UnexpectedJwtException extends JwtException {
    public UnexpectedJwtException(String message) {
        super(message);
    }
}
