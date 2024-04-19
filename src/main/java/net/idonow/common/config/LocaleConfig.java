package net.idonow.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class LocaleConfig {

    @Bean
    public Locale getDefaultLocale(AppConfig appConfig) {
        return new Locale.Builder()
                .setLanguage(appConfig.getAppDefaultLanguage())
                .setRegion(appConfig.getAppCountryCode())
                .build();
    }
}
