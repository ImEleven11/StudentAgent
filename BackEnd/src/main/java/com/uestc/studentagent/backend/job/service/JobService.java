package com.uestc.studentagent.backend.job.service;

import com.uestc.studentagent.backend.common.pagination.PageResponse;
import com.uestc.studentagent.backend.job.dto.JobDetailResponse;
import com.uestc.studentagent.backend.job.dto.JobItemResponse;
import com.uestc.studentagent.backend.job.dto.JobPathResponse;
import com.uestc.studentagent.backend.job.dto.JobSearchRequest;

public interface JobService {

    PageResponse<JobItemResponse> search(JobSearchRequest request);

    JobDetailResponse getDetail(Long jobId);

    JobPathResponse getPath(Long jobId);
}
