package net.idonow.common.api;

import net.idonow.common.util.TimestampUtils;
import org.springframework.http.HttpStatusCode;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.idonow.common.data.StringConstants.*;

public class ApiResponseHelpers {

    public static Map<String, Object> getDefaultErrorResponseBody(HttpStatusCode statusCode, String cause) {

        Map<String, Object> error = getDefaultResponseBody();
        error.put(STATUS, statusCode.value());
        error.put(CAUSE, cause);
        return error;
    }

    public static Map<String, Object> unsuccessfulAuthResponse(String cause, ApplicationErrorCode applicationErrorCode) {

        Map<String, Object> uAuth = getDefaultResponseBody();
        uAuth.put(CAUSE, cause);
        uAuth.put(APP_ERROR_CODE, applicationErrorCode.getCode());
        return uAuth;
    }

    private static Map<String, Object> getDefaultResponseBody() {

        Map<String, Object> defaultBody = new LinkedHashMap<>();
        defaultBody.put(TIMESTAMP, TimestampUtils.getTimestampZeroOffset());
        return defaultBody;
    }
}
