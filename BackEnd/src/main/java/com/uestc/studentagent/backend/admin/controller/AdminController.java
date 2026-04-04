package com.uestc.studentagent.backend.admin.controller;

import com.uestc.studentagent.backend.admin.dto.JobUploadResponse;
import com.uestc.studentagent.backend.admin.dto.RefreshProfilesResponse;
import com.uestc.studentagent.backend.admin.dto.SystemConfigRequest;
import com.uestc.studentagent.backend.admin.dto.SystemConfigResponse;
import com.uestc.studentagent.backend.admin.dto.SystemStatsResponse;
import com.uestc.studentagent.backend.admin.service.AdminService;
import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/jobs/import")
    @Operation(summary = "Import jobs from CSV")
    public ApiResponse<JobUploadResponse> importJobs(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                     @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(adminService.importJobs(principal.getUserId(), file));
    }

    @PostMapping("/jobs/refresh-profiles")
    @Operation(summary = "Trigger job profile refresh task")
    public ApiResponse<RefreshProfilesResponse> refreshProfiles(@AuthenticationPrincipal SecurityUserPrincipal principal) {
        return ApiResponse.success(adminService.refreshJobProfiles(principal.getUserId()));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get system usage statistics")
    public ApiResponse<SystemStatsResponse> stats(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                  @RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate) {
        return ApiResponse.success(adminService.stats(principal.getUserId(), startDate, endDate));
    }

    @PutMapping("/system/config")
    @Operation(summary = "Update system configuration")
    public ApiResponse<SystemConfigResponse> updateSystemConfig(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                                @Valid @RequestBody SystemConfigRequest request) {
        return ApiResponse.success(adminService.updateSystemConfig(principal.getUserId(), request));
    }
}
