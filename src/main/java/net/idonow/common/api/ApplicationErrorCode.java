package net.idonow.common.api;

public enum ApplicationErrorCode {

    INVALID_CREDENTIALS(1),
    ACCOUNT_NOT_ACTIVE(2),
    PHONE_NUMBER_NOT_VERIFIED(3),
    AUTHENTICATION_ERROR(4);

    private final int code;

    ApplicationErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
