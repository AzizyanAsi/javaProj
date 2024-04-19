package net.idonow.controller.exception.common;

public class BigDecimalScaleException extends RuntimeException {

    private final String fieldName;

    public BigDecimalScaleException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
