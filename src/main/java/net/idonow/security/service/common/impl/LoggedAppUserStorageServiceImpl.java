package net.idonow.security.service.common.impl;

import lombok.extern.slf4j.Slf4j;
import net.idonow.security.service.common.LoggedUserStorageService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static net.idonow.common.cache.TemplateCacheNames.APP_USER_ACCESS_TOKEN_CACHE_KEY;
import static net.idonow.common.cache.TemplateCacheNames.APP_USER_REFRESH_TOKEN_CACHE_KEY;

@Slf4j
@Service
public class LoggedAppUserStorageServiceImpl implements LoggedUserStorageService {

    private final HashOperations<String, String, String> redisHashOperations;

    public LoggedAppUserStorageServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisHashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void storeAccessToken(String email, String accessToken) {
        // If there is logged-in user with sent email change accessToken else add new (email, accessToken)
        redisHashOperations.put(APP_USER_ACCESS_TOKEN_CACHE_KEY, email, accessToken);
    }

    @Override
    public void storeRefreshToken(String email, String refreshToken) {
        redisHashOperations.put(APP_USER_REFRESH_TOKEN_CACHE_KEY, email, refreshToken);
    }

    @Override
    public boolean isInvalidAccessToken(String email, String accessToken) {
        String token = redisHashOperations.get(APP_USER_ACCESS_TOKEN_CACHE_KEY, email);
        return token == null || !token.equals(accessToken);
    }

    @Override
    public boolean isInvalidRefreshToken(String email, String refreshToken) {
        String token = redisHashOperations.get(APP_USER_REFRESH_TOKEN_CACHE_KEY, email);
        return token == null || !token.equals(refreshToken);
    }

    @Override
    public void interruptUserSession(String email) {
        redisHashOperations.delete(APP_USER_ACCESS_TOKEN_CACHE_KEY, email);
        redisHashOperations.delete(APP_USER_REFRESH_TOKEN_CACHE_KEY, email);
        log.info("User session interrupted: '{}'", email);
    }
}
