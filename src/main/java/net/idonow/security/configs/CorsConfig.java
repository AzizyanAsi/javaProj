package net.idonow.security.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "application.cors")
public class CorsConfig {

    List<String> hosts;
    List<Integer> ports;

    public CorsConfig() {
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    @Nullable
    List<String> getAllowedOrigins() {

        // NO host is set
        if (hosts == null || hosts.isEmpty()) {
            return null;
        }

        // NO port is configured (only hosts are set)
        if (ports == null || ports.isEmpty()) {
            return hosts;
        }

        // Ports are configured as 'cors.ports' - create combinations
        List<String> combinations = new ArrayList<>();
        for (String host : hosts) {
            for (int port : ports) {
                combinations.add(host + ":" + port);
            }
        }
        return combinations;
    }
}
