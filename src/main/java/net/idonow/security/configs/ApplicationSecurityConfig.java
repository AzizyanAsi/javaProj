package net.idonow.security.configs;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import net.idonow.common.util.LocaleUtils;
import net.idonow.security.service.AppUserDetailsService;
import net.idonow.security.service.SystemUserDetailsService;
import net.idonow.security.service.common.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

import static net.idonow.common.data.StringConstants.AUTHORIZATION;
import static net.idonow.common.data.StringConstants.REFRESH_TOKEN;
import static net.idonow.security.configs.dsl.AppUserDsl.appUserDsl;
import static net.idonow.security.configs.dsl.SystemUserDsl.systemUserDsl;
import static net.idonow.security.enums.RoleType.*;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ApplicationSecurityConfig {

    private final AppUserDetailsService appUserDetailsService;
    private final SystemUserDetailsService systemUserDetailsService;
    private final JwtService jwtService;
    private final LogoutHandler logoutHandler;
    private PasswordEncoder passwordEncoder;
    private CorsConfig corsConfig;
    private LocaleUtils localeUtils;
    private Environment environment;

    public ApplicationSecurityConfig(AppUserDetailsService appUserDetailsService,
                                     SystemUserDetailsService systemUserDetailsService,
                                     JwtService jwtService,
                                     LogoutHandler logoutHandler) {
        this.appUserDetailsService = appUserDetailsService;
        this.systemUserDetailsService = systemUserDetailsService;
        this.jwtService = jwtService;
        this.logoutHandler = logoutHandler;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setCorsConfig(CorsConfig corsConfig) {
        this.corsConfig = corsConfig;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain appUserFilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/api/**")
                .authorizeHttpRequests((registry) -> registry
                .requestMatchers(HttpMethod.GET,
                        "/api/countries/**",
                        "/api/info"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/token/refresh",
                        "/api/password-check",
                        "/api/users/forgot-password",
                        "/api/users/forgot-password/verify",
                        "/api/users/reset-password",
                        "/api/users/registration",
                        "/api/users/phone-number/start-verification",
                        "/api/users/phone-number/confirm-verification"
                ).permitAll()
                .requestMatchers("/api/**").hasAnyRole(CLIENT.name(), PROFESSIONAL.name())
                .anyRequest().authenticated()
        );

        http.apply(appUserDsl(jwtService, localeUtils));

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.headers((headers) -> headers
                .xssProtection(Customizer.withDefaults())
                .contentSecurityPolicy((contentSecurityPolicy) -> contentSecurityPolicy.policyDirectives("script-src 'self'")));

        http.logout((logout) -> logout
                .logoutUrl("/api/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()));

        http.authenticationProvider(appUserAuthenticationProvider());

        return http.build();
    }

    @Bean
    public SecurityFilterChain systemUserFilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/system/**")
                .authorizeHttpRequests((registry) -> registry
                .requestMatchers(HttpMethod.GET,
                        "/system/actuator/health"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "system/token/refresh",
                        "system/systemUser/registration",
                        "system/systemUser/forgot-password",
                        "system/systemUser/forgot-password/verify",
                        "system/systemUser/reset-password"
                ).permitAll()
                .requestMatchers("/system/roles").hasRole(ADMIN.name())
                .requestMatchers("/system/**").hasAnyRole(ADMIN.name(), SUPPORT_AGENT.name())
                .anyRequest().authenticated()
        );

        http.apply(systemUserDsl(jwtService, localeUtils));

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.headers((headers) -> headers
                .xssProtection(Customizer.withDefaults())
                .contentSecurityPolicy((contentSecurityPolicy) -> contentSecurityPolicy.policyDirectives("script-src 'self'")));

        http.logout((logout) -> logout
                .logoutUrl("/system/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()));

        http.authenticationProvider(systemUserAuthenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider appUserAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(appUserDetailsService);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationProvider systemUserAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(systemUserDetailsService);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .authenticationProvider(appUserAuthenticationProvider())
                .authenticationProvider(systemUserAuthenticationProvider());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();

        // Set origins depending on profile
        String[] profiles = environment.getActiveProfiles();
        // Allow all if active profile is 'dev', otherwise - fetch from config 
        if (profiles.length > 0 && Arrays.asList(profiles).contains("dev")) {
            configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        } else {
            configuration.setAllowedOrigins(corsConfig.getAllowedOrigins());
        }
        configuration.setAllowedMethods(ImmutableList.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.setAllowCredentials(true);
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.setAllowedHeaders(ImmutableList.of(AUTHORIZATION, REFRESH_TOKEN, "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(ImmutableList.of(AUTHORIZATION, REFRESH_TOKEN));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS is enabled (see active profile)");
        return source;
    }
}
