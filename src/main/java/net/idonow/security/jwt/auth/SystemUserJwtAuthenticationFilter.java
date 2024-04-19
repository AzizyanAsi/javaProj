package net.idonow.security.jwt.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.idonow.common.api.ApiResponseHelpers;
import net.idonow.common.api.ApplicationErrorCode;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.exception.security.AccountNotActiveException;
import net.idonow.controller.exception.security.EmailNotFoundException;
import net.idonow.controller.exception.security.PhoneNumberNotFoundException;
import net.idonow.security.jwt.common.AuthenticationRequest;
import net.idonow.security.service.common.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class SystemUserJwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    // !!! Log for this class may be filtered out to disable unsuccessful authentication stacktrace logging

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LocaleUtils localeUtils;
    private final ObjectMapper objectMapper;

    public SystemUserJwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                             JwtService jwtService,
                                             LocaleUtils localeUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.localeUtils = localeUtils;
        this.objectMapper = new ObjectMapper();
        this.setFilterProcessesUrl("/system/login");
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        AuthenticationRequest authenticationRequest;
        try {
            authenticationRequest = objectMapper
                    .readValue(request.getInputStream(), AuthenticationRequest.class);
        } catch (IOException e) {
            throw new RuntimeException("Malformed request body");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authenticationRequest.getUsername(),
                authenticationRequest.getPassword()
        );
        return authenticationManager.authenticate(authentication);

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        HttpStatus status = HttpStatus.FORBIDDEN;
        Map<String, Object> unsuccessfulAuth = null;

        if (failed.getClass().equals(BadCredentialsException.class) || (failed.getCause() != null &&
                (failed.getCause().getClass().equals(EmailNotFoundException.class) || failed.getCause().getClass().equals(PhoneNumberNotFoundException.class)))) {

            unsuccessfulAuth = ApiResponseHelpers.unsuccessfulAuthResponse(
                    localeUtils.getLocalizedMessage("error.invalid-user-pwd"), ApplicationErrorCode.INVALID_CREDENTIALS
            );

        } else if (failed.getClass().equals(InternalAuthenticationServiceException.class)) {
            String message = "Internal authentication error";
            if (failed.getCause() != null && failed.getCause().getClass().equals(AccountNotActiveException.class)) {

                unsuccessfulAuth = ApiResponseHelpers.unsuccessfulAuthResponse(
                        message, ApplicationErrorCode.ACCOUNT_NOT_ACTIVE
                );
            }
        } else {
            unsuccessfulAuth = ApiResponseHelpers.unsuccessfulAuthResponse("Unknown authentication error", ApplicationErrorCode.AUTHENTICATION_ERROR);
        }

        response.setStatus(status.value());
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
                .println(objectMapper.writeValueAsString(unsuccessfulAuth));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) {

        // After successful authentication store refresh token with LoggedUserStorageService
        // This will ensure that server will authenticate requests only with  above access token and new tokens pair will be given only with above refresh token
        jwtService.provideTokens(authResult.getName(), authResult.getAuthorities(), request, response);
    }

}
