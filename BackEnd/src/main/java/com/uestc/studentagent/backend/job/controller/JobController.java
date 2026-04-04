package com.uestc.studentagent.backend.job.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.common.pagination.PageResponse;
import com.uestc.studentagent.backend.job.dto.JobDetailResponse;
import com.uestc.studentagent.backend.job.dto.JobItemResponse;
import com.uestc.studentagent.backend.job.dto.JobPathResponse;
import com.uestc.studentagent.backend.job.dto.JobSearchRequest;
import com.uestc.studentagent.backend.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job")
@Tag(name = "Job")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search jobs")
    public ApiResponse<PageResponse<JobItemResponse>> search(JobSearchRequest request) {
        return ApiResponse.success(jobService.search(request));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get job detail")
    public ApiResponse<JobDetailResponse> getDetail(@PathVariable Long jobId) {
        return ApiResponse.success(jobService.getDetail(jobId));
    }

    @GetMapping("/{jobId}/path")
    @Operation(summary = "Get job development path")
    public ApiResponse<JobPathResponse> getPath(@PathVariable Long jobId) {
        return ApiResponse.success(jobService.getPath(jobId));
    }
}
