package net.idonow.common.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "application.storage")
public class StorageConfig {

    private String bucketName;
    private String thumbnailDirectory;
    private String profilePictureDirectory;
    private String workingSampleDirectory;
    private String resumeDirectory;
    private List<String> imageAllowedContentTypes;
    private List<String> documentAllowedContentTypes;
    private List<String> allowedExtensions;
    private String urlTemplate;

    public String getWorkingSamplesThumbnailDirectory() {
        return thumbnailDirectory + workingSampleDirectory;
    }

}
