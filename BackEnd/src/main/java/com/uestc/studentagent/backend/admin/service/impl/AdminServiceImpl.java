package com.uestc.studentagent.backend.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uestc.studentagent.backend.admin.dto.JobUploadResponse;
import com.uestc.studentagent.backend.admin.dto.RefreshProfilesResponse;
import com.uestc.studentagent.backend.admin.dto.SystemConfigRequest;
import com.uestc.studentagent.backend.admin.dto.SystemConfigResponse;
import com.uestc.studentagent.backend.admin.dto.SystemStatsResponse;
import com.uestc.studentagent.backend.admin.entity.SystemConfigEntity;
import com.uestc.studentagent.backend.admin.mapper.SystemConfigMapper;
import com.uestc.studentagent.backend.admin.service.AdminService;
import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.job.entity.JobEntity;
import com.uestc.studentagent.backend.job.mapper.JobMapper;
import com.uestc.studentagent.backend.user.entity.UserEntity;
import com.uestc.studentagent.backend.user.entity.UserRole;
import com.uestc.studentagent.backend.user.service.UserService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserService userService;
    private final JobMapper jobMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AdminServiceImpl(UserService userService,
                            JobMapper jobMapper,
                            SystemConfigMapper systemConfigMapper,
                            JdbcTemplate jdbcTemplate,
                            ObjectMapper objectMapper) {
        this.userService = userService;
        this.jobMapper = jobMapper;
        this.systemConfigMapper = systemConfigMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public JobUploadResponse importJobs(Long userId, MultipartFile file) {
        requireAdmin(userId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "upload file is required");
        }
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".csv")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "currently only CSV upload is supported");
        }

        int importedCount = 0;
        int failedCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                if (!StringUtils.hasText(line)) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 4) {
                    failedCount++;
                    continue;
                }
                try {
                    JobEntity entity = new JobEntity();
                    entity.setTitle(parts[0].trim());
                    entity.setIndustry(parts[1].trim());
                    entity.setLocation(parts[2].trim());
                    entity.setDescription(parts[3].trim());
                    entity.setSalaryMin(parts.length > 4 && StringUtils.hasText(parts[4]) ? new BigDecimal(parts[4].trim()) : null);
                    entity.setSalaryMax(parts.length > 5 && StringUtils.hasText(parts[5]) ? new BigDecimal(parts[5].trim()) : null);
                    entity.setSkillsJson(parts.length > 6 ? toJson(parseSkills(parts[6])) : "[]");
                    entity.setPortraitSummary(parts.length > 7 ? parts[7].trim() : null);
                    jobMapper.insert(entity);
                    importedCount++;
                } catch (Exception ignored) {
                    failedCount++;
                }
            }
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to read uploaded csv");
        }

        return new JobUploadResponse(importedCount, failedCount);
    }

    @Override
    public RefreshProfilesResponse refreshJobProfiles(Long userId) {
        requireAdmin(userId);
        return new RefreshProfilesResponse("refresh-job-profiles-" + UUID.randomUUID());
    }

    @Override
    public SystemStatsResponse stats(Long userId, String startDate, String endDate) {
        requireAdmin(userId);
        return new SystemStatsResponse(
                count("SELECT COUNT(1) FROM users"),
                count("SELECT COUNT(1) FROM student_profiles"),
                count("SELECT COUNT(1) FROM match_records"),
                count("SELECT COUNT(1) FROM plans"),
                count("SELECT COUNT(1) FROM reports")
        );
    }

    @Override
    @Transactional
    public SystemConfigResponse updateSystemConfig(Long userId, SystemConfigRequest request) {
        requireAdmin(userId);
        SystemConfigEntity entity = systemConfigMapper.findByKey(request.getConfigKey().trim());
        if (entity == null) {
            entity = new SystemConfigEntity();
            entity.setConfigKey(request.getConfigKey().trim());
            entity.setConfigValue(request.getConfigValue().trim());
            systemConfigMapper.insert(entity);
            entity = systemConfigMapper.findByKey(request.getConfigKey().trim());
        } else {
            entity.setConfigValue(request.getConfigValue().trim());
            systemConfigMapper.update(entity);
            entity = systemConfigMapper.findByKey(request.getConfigKey().trim());
        }
        return new SystemConfigResponse(entity.getConfigKey(), entity.getConfigValue(), entity.getUpdatedAt());
    }

    private UserEntity requireAdmin(Long userId) {
        UserEntity user = userService.loadActiveUser(userId);
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "admin permission is required");
        }
        return user;
    }

    private long count(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }

    private List<String> parseSkills(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        String[] values = raw.split("\\|");
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                result.add(value.trim().toLowerCase());
            }
        }
        return result;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to serialize job skills");
        }
    }
}
