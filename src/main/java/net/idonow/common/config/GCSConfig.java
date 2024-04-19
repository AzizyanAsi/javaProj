package net.idonow.common.config;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "application.gcs")
public class GCSConfig {

    private String projectId;
    private String credentialsPath;

    @Bean
    public Storage googleStorage() throws IOException {
        Credentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsPath));
        return StorageOptions.newBuilder().setCredentials(credentials)
                .setProjectId(credentialsPath).build().getService();
    }

}
