package com.uestc.studentagent.backend.admin.service;

import com.uestc.studentagent.backend.admin.dto.JobUploadResponse;
import com.uestc.studentagent.backend.admin.dto.RefreshProfilesResponse;
import com.uestc.studentagent.backend.admin.dto.SystemConfigRequest;
import com.uestc.studentagent.backend.admin.dto.SystemConfigResponse;
import com.uestc.studentagent.backend.admin.dto.SystemStatsResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AdminService {

    JobUploadResponse importJobs(Long userId, MultipartFile file);

    RefreshProfilesResponse refreshJobProfiles(Long userId);

    SystemStatsResponse stats(Long userId, String startDate, String endDate);

    SystemConfigResponse updateSystemConfig(Long userId, SystemConfigRequest request);
}
