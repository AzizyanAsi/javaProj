package net.idonow.transform.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private String originalImageUrl;
    private String thumbnailImageUrl;
    private Integer ordinal;
}
