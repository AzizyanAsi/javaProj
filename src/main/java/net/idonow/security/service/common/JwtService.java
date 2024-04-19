package net.idonow.security.service.common;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface JwtService {

    Authentication decodeAccessJwt(String accessToken, HttpServletRequest request) throws JwtException;

    String decodeRefreshJwt(HttpServletRequest request);

    void provideTokens(String email, Collection<? extends GrantedAuthority> authorities, HttpServletRequest request, HttpServletResponse response);

    boolean isAppUser(String requestURI);

}
