package net.idonow.service.common.impl;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import net.idonow.common.config.StorageConfig;
import net.idonow.controller.exception.common.ActionNotAllowedException;
import net.idonow.service.common.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    private final Storage storage;
    private final StorageConfig storageConfig;

    public StorageServiceImpl(Storage storage, StorageConfig storageConfig) {
        this.storage = storage;
        this.storageConfig = storageConfig;
    }

    @Override
    public String createFileName(MultipartFile multipartFile) {
        String originalName = Objects.requireNonNull(multipartFile.getOriginalFilename());
        return UUID.randomUUID() + originalName.substring(originalName.lastIndexOf("."));
    }

    @Override
    public List<String> createNewImageNamesForOrderedSequence(List<MultipartFile> newImages, Set<String> existingImageNames) {
        // List of names of images to be added
        List<String> imageNames = new ArrayList<>();

        // Image sequence in the set is organized by adding numbers at the end of the image name (ex` UUID_10, UUID_20)
        int currentSuffix = 0;
        if (existingImageNames != null && !existingImageNames.isEmpty()) {
            currentSuffix = Collections.max(
                    existingImageNames.stream()
                            .map(s -> Integer.parseInt(s.substring(s.lastIndexOf("_") + 1, s.lastIndexOf(".")))).toList()
            );
        }
        // Generate names for images
        for (MultipartFile image : newImages) {
            // The following suffix is greater than the previous one with fixed value
            currentSuffix += 10;
            String createdName = createFileName(image);
            int insertionIndex = createdName.lastIndexOf(".");
            // Insert suffix to image created bellow
            createdName = createdName.substring(0, insertionIndex) + "_" + currentSuffix + createdName.substring(insertionIndex);
            imageNames.add(createdName);
        }

        return imageNames;
    }

    @Override
    public String uploadDocument(MultipartFile multipartFile, String directory, String documentName) throws IOException {
        String contentType = multipartFile.getContentType();
        validateContentType(contentType, storageConfig.getDocumentAllowedContentTypes());
        BlobId blobId = BlobId.of(storageConfig.getBucketName(), directory + documentName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        storage.create(blobInfo, multipartFile.getBytes());
        log.info("Document successfully uploaded to storage: {}", blobId.getName());
        return storageConfig.getUrlTemplate() + blobId.getName();
    }

    @Override
    public String uploadImage(MultipartFile multipartFile, String directory, String imageName, boolean asThumbnail) throws IOException {
        String contentType = multipartFile.getContentType();
        validateContentType(contentType, storageConfig.getImageAllowedContentTypes());
        BlobId blobId = BlobId.of(storageConfig.getBucketName(), directory + imageName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        if (!asThumbnail) {
            storage.create(blobInfo, resizeOriginalImage(multipartFile, 720, 1280, false).toByteArray());
        } else {
            storage.create(blobInfo, resizeOriginalImage(multipartFile, 300, 300, true).toByteArray()); // TODO fix the size
        }
        log.info("Image successfully uploaded to storage: {}", blobId.getName());
        return storageConfig.getUrlTemplate() + blobId.getName();
    }


    @Override
    public void deleteObject(String directory, String name) {
        BlobId blobId = BlobId.of(storageConfig.getBucketName(), directory + name);
        if (storage.delete(blobId)) {
            log.info("Object deleted: {}", blobId.getName());
        } else {
            log.warn("File with name '{}' does not exist in cloud storage", blobId.getName());
        }
    }

    /*  -- PRIVATE METHODS -- */

    // Check that the image content type is supported
    private void validateContentType(String contentType, List<String> allowedContentTypes) {
        if (contentType != null && allowedContentTypes.contains(contentType)) {
            return;
        }
        throw new ActionNotAllowedException("Content type is not supported");
    }

    private ByteArrayOutputStream resizeOriginalImage(MultipartFile originalFile, int width, int height, boolean resizeToExactlySpecifiedSize) throws IOException {
        ByteArrayOutputStream resizedImgOutput = new ByteArrayOutputStream();
        String originalName = Objects.requireNonNull(originalFile.getOriginalFilename());
        String outputFormat = originalName.substring(originalName.lastIndexOf(".") + 1);

        if (resizeToExactlySpecifiedSize) {
            Thumbnails.of(originalFile.getInputStream())
                    .crop(Positions.CENTER)
                    .size(width, height)
                    .outputQuality(1)
                    .outputFormat(outputFormat)
                    .toOutputStream(resizedImgOutput);
        } else {
            Thumbnails.of(originalFile.getInputStream())
                    .size(width, height)
                    .outputQuality(1)
                    .outputFormat(outputFormat)
                    .toOutputStream(resizedImgOutput);
        }
        return resizedImgOutput;
    }
}
