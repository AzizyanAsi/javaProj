package net.idonow.security.configs.dsl;

import net.idonow.common.util.LocaleUtils;
import net.idonow.security.jwt.auth.AppUserJwtAuthenticationFilter;
import net.idonow.security.jwt.verify.JwtVerifier;
import net.idonow.security.service.common.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class AppUserDsl extends AbstractHttpConfigurer<AppUserDsl, HttpSecurity> {

    private final JwtService jwtService;

    private final LocaleUtils localeUtils;

    public AppUserDsl(JwtService jwtService, LocaleUtils localeUtils) {
        this.jwtService = jwtService;
        this.localeUtils = localeUtils;
    }

    @Override
    public void configure(HttpSecurity http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http.addFilterBefore(new AppUserJwtAuthenticationFilter(authenticationManager, jwtService, localeUtils), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new JwtVerifier(jwtService), AppUserJwtAuthenticationFilter.class);
    }

    public static AppUserDsl appUserDsl(JwtService jwtService, LocaleUtils localeUtils) {
        return new AppUserDsl(jwtService, localeUtils);
    }

}
