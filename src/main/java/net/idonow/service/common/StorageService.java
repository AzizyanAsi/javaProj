package net.idonow.service.common;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface StorageService {
    String createFileName(MultipartFile multipartFile);
    List<String> createNewImageNamesForOrderedSequence(List<MultipartFile> newImages, Set<String> existingImageNames);
    String uploadDocument(MultipartFile multipartFile, String directory, String documentName) throws IOException;
    String uploadImage(MultipartFile multipartFile, String directory, String imageName, boolean asThumbnail) throws IOException;
    void deleteObject(String directory, String name);
}
