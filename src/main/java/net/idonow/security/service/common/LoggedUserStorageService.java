package net.idonow.security.service.common;

public interface LoggedUserStorageService {

    void storeAccessToken(String key, String value);

    void storeRefreshToken(String key, String value);

    boolean isInvalidAccessToken(String email, String accessToken);

    boolean isInvalidRefreshToken(String email, String refreshToken);

    void interruptUserSession(String email);
}
