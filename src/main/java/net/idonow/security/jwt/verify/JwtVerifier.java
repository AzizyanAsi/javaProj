package net.idonow.security.jwt.verify;

import com.google.common.base.Strings;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.idonow.security.service.common.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static net.idonow.common.data.StringConstants.AUTHORIZATION;
import static net.idonow.common.data.StringConstants.JWT_PREFIX;

public class JwtVerifier extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtVerifier(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (Strings.isNullOrEmpty(authorizationHeader) || !authorizationHeader.startsWith(JWT_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        // Prefix is already checked above
        String token = authorizationHeader.substring(JWT_PREFIX.length()).stripLeading();

        try {
            Authentication authentication = jwtService.decodeAccessJwt(token, request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException e) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
