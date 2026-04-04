package com.uestc.studentagent.backend.common.storage;

import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.config.LocalStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".doc", ".docx");

    private final LocalStorageProperties storageProperties;

    public LocalFileStorageService(LocalStorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public StoredFile storeResume(Long userId, MultipartFile file) {
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String safeFileName = sanitizeFileName(originalFileName);
        String extension = extractExtension(safeFileName);
        String storedName = UUID.randomUUID() + extension;

        try {
            Path root = Path.of(storageProperties.getLocalRoot()).toAbsolutePath().normalize();
            Path userDir = root.resolve("resumes").resolve(String.valueOf(userId)).normalize();
            Files.createDirectories(userDir);

            Path target = userDir.resolve(storedName).normalize();
            if (!target.startsWith(root)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "invalid file path");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return new StoredFile(
                    originalFileName,
                    file.getContentType(),
                    target.toString(),
                    file.getSize()
            );
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to store resume file");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "resume file is required");
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "only PDF, DOC, and DOCX files are allowed");
        }
    }

    private String sanitizeFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            return "resume";
        }
        return originalFileName.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String extractExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        return index >= 0 ? fileName.substring(index) : "";
    }
}
