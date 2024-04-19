package net.idonow.common.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.idonow.common.config.StorageConfig;
import net.idonow.common.validation.constraints.ValidMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileValidator implements ConstraintValidator<ValidMultipartFile, MultipartFile> {

    private StorageConfig storageConfig;

    @Autowired
    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @Override
    public void initialize(ValidMultipartFile validMultipartFile) {
    }

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && !value.isEmpty() && value.getOriginalFilename() != null && fileNameEndsWithAllowedExtension(value.getOriginalFilename());
    }

    private boolean fileNameEndsWithAllowedExtension(String fileName) {
        for (String extension : storageConfig.getAllowedExtensions()) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}
