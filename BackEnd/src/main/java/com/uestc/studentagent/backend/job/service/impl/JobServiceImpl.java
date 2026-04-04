package com.uestc.studentagent.backend.job.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.common.pagination.PageResponse;
import com.uestc.studentagent.backend.job.dto.JobDetailResponse;
import com.uestc.studentagent.backend.job.dto.JobItemResponse;
import com.uestc.studentagent.backend.job.dto.JobPathNodeResponse;
import com.uestc.studentagent.backend.job.dto.JobPathResponse;
import com.uestc.studentagent.backend.job.dto.JobSearchRequest;
import com.uestc.studentagent.backend.job.entity.JobEntity;
import com.uestc.studentagent.backend.job.entity.JobPathType;
import com.uestc.studentagent.backend.job.mapper.JobMapper;
import com.uestc.studentagent.backend.job.mapper.JobPathMapper;
import com.uestc.studentagent.backend.job.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final JobMapper jobMapper;
    private final JobPathMapper jobPathMapper;
    private final ObjectMapper objectMapper;

    public JobServiceImpl(JobMapper jobMapper, JobPathMapper jobPathMapper, ObjectMapper objectMapper) {
        this.jobMapper = jobMapper;
        this.jobPathMapper = jobPathMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResponse<JobItemResponse> search(JobSearchRequest request) {
        int page = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int size = request.getSize() == null || request.getSize() < 1 ? 10 : Math.min(request.getSize(), 100);
        int offset = (page - 1) * size;

        List<JobItemResponse> records = jobMapper.search(
                        normalize(request.getKeyword()),
                        normalize(request.getIndustry()),
                        normalize(request.getLocation()),
                        offset,
                        size
                ).stream()
                .map(JobItemResponse::from)
                .toList();
        long total = jobMapper.countSearch(
                normalize(request.getKeyword()),
                normalize(request.getIndustry()),
                normalize(request.getLocation())
        );
        return new PageResponse<>(page, size, total, records);
    }

    @Override
    public JobDetailResponse getDetail(Long jobId) {
        JobEntity job = requireJob(jobId);
        return JobDetailResponse.from(job, readSkills(job.getSkillsJson()));
    }

    @Override
    public JobPathResponse getPath(Long jobId) {
        requireJob(jobId);
        List<JobPathNodeResponse> promotionPaths = jobPathMapper.findByJobId(jobId).stream()
                .filter(path -> path.getPathType() == JobPathType.PROMOTION)
                .map(JobPathNodeResponse::from)
                .toList();
        List<JobPathNodeResponse> transitionPaths = jobPathMapper.findByJobId(jobId).stream()
                .filter(path -> path.getPathType() == JobPathType.TRANSITION)
                .map(JobPathNodeResponse::from)
                .toList();

        return new JobPathResponse(jobId, promotionPaths, transitionPaths);
    }

    private JobEntity requireJob(Long jobId) {
        JobEntity job = jobMapper.findById(jobId);
        if (job == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "job not found");
        }
        return job;
    }

    private List<String> readSkills(String skillsJson) {
        if (!StringUtils.hasText(skillsJson)) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(skillsJson, STRING_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to parse job skills");
        }
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
