package net.idonow.entity.enums;

public enum TokenType {
    PASSWORD_RESET(true), EMAIL_CONFIRM(true);

    private final boolean expirable;

    TokenType(boolean expirable) {
        this.expirable = expirable;
    }

    public boolean isExpirable() {
        return expirable;
    }
}

