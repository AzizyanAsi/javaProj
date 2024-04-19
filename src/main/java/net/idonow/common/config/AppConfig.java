package net.idonow.common.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "application.common")
public class AppConfig {

    private String version;
    private String appCountryCode;
    private String appDefaultLanguage;

    public String getVersion() {
        // If not set by POM (dev mode) - try to get from MANIFEST (jar bundle)
        if (version == null) {
            version = this.getClass().getPackage().getImplementationVersion();
        }
        return version;
    }
}
