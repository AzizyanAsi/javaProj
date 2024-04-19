package net.idonow.security.jwt.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "application.jwt")
public class JwtConfig {

    private String accessSecretKey;
    private String refreshSecretKey;
    private Integer accessTokenExpirationAfterMinutes;
    private Integer refreshTokenExpirationAfterDays;
}
