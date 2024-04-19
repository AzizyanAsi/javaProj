package net.idonow.common.api;

import lombok.Getter;
import lombok.Setter;
import net.idonow.common.util.TimestampUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
public class ApiResponse<T> extends ResponseEntity<ApiResponse.Body<T>> {

    private ApiResponse(HttpStatus status, Body<T> body) {
        super(body, status);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(HttpStatus.OK, bodyOf(data, message));
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(HttpStatus.OK, bodyOf(message));
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status, T data) {
        return new ApiResponse<>(status, bodyOf(data, message));
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, bodyOf(data, message));
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, bodyOf(message));
    }

    /* -- HELPER METHODS -- */

    private static <T> Body<T> bodyOf(T data, String message) {
        return new Body<>(data, message);
    }

    private static <T> Body<T> bodyOf(String message) {
        return new Body<>(message);
    }

    /* -- NESTED CLASSES -- */

    @Getter
    public static class Body<T> {

        private final String timestamp;
        private final String message;
        private T data;

        public Body(T data, String message) {
            this.timestamp = TimestampUtils.getTimestampZeroOffset();
            this.message = message;
            this.data = data;
        }

        public Body(String message) {
            this.timestamp = TimestampUtils.getTimestampZeroOffset();
            this.message = message;
        }
    }
}
