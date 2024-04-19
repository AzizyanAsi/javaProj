package net.idonow.common.config;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonSpatialConfig {

    @Bean
    public JtsModule jtsModule() {
        return new JtsModule();
    }
}
