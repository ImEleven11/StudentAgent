package com.uestc.studentagent.backend.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile storeResume(Long userId, MultipartFile file);
}
