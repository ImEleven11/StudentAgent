package com.uestc.studentagent.backend.common.storage;

public record StoredFile(
        String originalFileName,
        String contentType,
        String storagePath,
        long size
) {
}
