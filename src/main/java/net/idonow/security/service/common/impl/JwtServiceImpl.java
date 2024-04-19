package net.idonow.security.service.common.impl;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.idonow.controller.exception.common.ActionNotAllowedException;
import net.idonow.controller.exception.security.UnexpectedJwtException;
import net.idonow.security.jwt.common.JwtConfig;
import net.idonow.security.service.common.JwtService;
import net.idonow.security.service.common.LoggedUserStorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static net.idonow.common.data.StringConstants.*;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final JwtConfig jwtConfig;
    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;
    private final LoggedUserStorageService loggedAppUserStorageService;
    private final LoggedUserStorageService loggedSystemUserStorageService;

    public JwtServiceImpl(JwtConfig jwtConfig,
                          @Qualifier("accessSecretKey") SecretKey accessSecretKey,
                          @Qualifier("refreshSecretKey") SecretKey refreshSecretKey,
                          @Qualifier("loggedAppUserStorageServiceImpl") LoggedUserStorageService loggedAppUserStorageService,
                          @Qualifier("loggedSystemUserStorageServiceImpl") LoggedUserStorageService loggedSystemUserStorageService) {
        this.jwtConfig = jwtConfig;
        this.accessSecretKey = accessSecretKey;
        this.refreshSecretKey = refreshSecretKey;
        this.loggedAppUserStorageService = loggedAppUserStorageService;
        this.loggedSystemUserStorageService = loggedSystemUserStorageService;
    }

    @Override
    public Authentication decodeAccessJwt(String accessToken, final HttpServletRequest request) throws JwtException {
        Jws<Claims> claimsJws = Jwts
                .parserBuilder()
                .setSigningKey(accessSecretKey)
                .build()
                .parseClaimsJws(accessToken);

        Claims body = claimsJws.getBody();

        String email = body.getSubject();

        // After successful decoding check if it is access token that expected
        boolean isInvalidToken;
        if (isAppUser(request.getRequestURI())){
            isInvalidToken = loggedAppUserStorageService.isInvalidAccessToken(email, accessToken);
        } else {
            isInvalidToken = loggedSystemUserStorageService.isInvalidAccessToken(email, accessToken);
        }
        if (isInvalidToken) {
            throw new UnexpectedJwtException(String.format("Provided unexpected token: '%s'", email));
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> authorities = (List<Map<String, String>>) body.get(AUTHORITIES);

        Set<SimpleGrantedAuthority> simpleGrantedAuthorities = authorities.stream()
                .map(m -> new SimpleGrantedAuthority(m.get("authority")))
                .collect(Collectors.toSet());

        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                simpleGrantedAuthorities
        );
    }

    @Override
    public String decodeRefreshJwt(final HttpServletRequest request) {
        String refreshToken = request.getHeader(REFRESH_TOKEN);
        if (Strings.isNullOrEmpty(refreshToken) || !refreshToken.startsWith(JWT_PREFIX)) {
            throw new ActionNotAllowedException("Refresh token can't be read from request header");
        }
        refreshToken = refreshToken.substring(JWT_PREFIX.length()).stripLeading();

        String email;
        try {
            Jws<Claims> claimsJws = Jwts
                    .parserBuilder()
                    .setSigningKey(refreshSecretKey)
                    .build()
                    .parseClaimsJws(refreshToken);
            email = claimsJws.getBody().getSubject();
        } catch (JwtException exception) {
            String message = "Refresh token cannot be trusted";
            log.warn(message + ".Cause: {}", exception.getMessage());
            throw new ActionNotAllowedException(message);
        }

        // After successful decoding check if it is refresh token that expected
        boolean isInvalidToken;
        if (isAppUser(request.getRequestURI())) {
            isInvalidToken = loggedAppUserStorageService.isInvalidRefreshToken(email, refreshToken);
        } else {
            isInvalidToken = loggedSystemUserStorageService.isInvalidRefreshToken(email, refreshToken);
        }
        if (isInvalidToken) {
            String message = "Invalid refresh token";
            log.warn(message + " [Issuer: '{}' (Valid, but not excepted)]", email);
            throw new ActionNotAllowedException(message);
        }
        return email;
    }

    @Override
    public void provideTokens(String email, Collection<? extends GrantedAuthority> authorities, final HttpServletRequest request, HttpServletResponse response) {

        // Access token contains all information server needs to know if the user can access the resource he is requesting or not
        // Refresh token is used to generate new access token
        AuthToken authToken = this.getTokens(email, authorities, request.getRequestURI());

        // Save new access and refresh tokens
        if (isAppUser(request.getRequestURI())) {
            loggedAppUserStorageService.storeAccessToken(email, authToken.accessToken());
            loggedAppUserStorageService.storeRefreshToken(email, authToken.refreshToken());
        } else {
            loggedSystemUserStorageService.storeAccessToken(email, authToken.accessToken());
            loggedSystemUserStorageService.storeRefreshToken(email, authToken.refreshToken());
        }
        response.setHeader(AUTHORIZATION, authToken.accessToken());
        response.setHeader(REFRESH_TOKEN, authToken.refreshToken());
    }

    @Override
    public boolean isAppUser(String requestURI) {
        return requestURI.startsWith("/api");
    }

    /* PRIVATE METHODS */

    private AuthToken getTokens(String email, Collection<? extends GrantedAuthority> authorities, String requestURI) {
        String accessToken = Jwts.builder()
                .setSubject(email)
                .claim(AUTHORITIES, authorities)
                .setIssuer(requestURI)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(jwtConfig.getAccessTokenExpirationAfterMinutes(), MINUTES)))
                .signWith(accessSecretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(email)
                .setIssuer(requestURI)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(jwtConfig.getRefreshTokenExpirationAfterDays(), DAYS)))
                .signWith(refreshSecretKey)
                .compact();
        AuthToken authToken = new AuthToken(accessToken, refreshToken);
        boolean isLoginRequest = requestURI.endsWith("/login");
        boolean isAppUser = isAppUser(requestURI);
        if (isAppUser) {
            log.info("New tokens provided to app user: '{}'", email);
            if (isLoginRequest) {
                log.info("User authenticated: '{}'", email);
            }
        } else {
            log.info("New tokens provided to system user: '{}'", email);
            if (isLoginRequest) {
                log.info("System user authenticated: '{}'", email);
            }
        }
        return authToken;
    }

    private record AuthToken(String accessToken, String refreshToken) {
    }

}
