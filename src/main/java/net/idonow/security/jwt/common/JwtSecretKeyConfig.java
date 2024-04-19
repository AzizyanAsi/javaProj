package net.idonow.security.jwt.common;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtSecretKeyConfig {

    private final JwtConfig jwtConfig;

    public JwtSecretKeyConfig(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Bean
    public SecretKey accessSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getAccessSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Bean
    public SecretKey refreshSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getRefreshSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
