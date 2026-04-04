package com.uestc.studentagent.backend.report.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.job.entity.JobEntity;
import com.uestc.studentagent.backend.job.mapper.JobMapper;
import com.uestc.studentagent.backend.profile.entity.StudentProfileEntity;
import com.uestc.studentagent.backend.profile.mapper.StudentProfileMapper;
import com.uestc.studentagent.backend.report.dto.ReportGenerateRequest;
import com.uestc.studentagent.backend.report.dto.ReportGenerateResponse;
import com.uestc.studentagent.backend.report.dto.ReportListItemResponse;
import com.uestc.studentagent.backend.report.dto.ReportResponse;
import com.uestc.studentagent.backend.report.dto.ReportSectionResponse;
import com.uestc.studentagent.backend.report.entity.ReportEntity;
import com.uestc.studentagent.backend.report.mapper.ReportMapper;
import com.uestc.studentagent.backend.report.service.ReportService;
import com.uestc.studentagent.backend.user.service.UserService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<ReportSectionResponse>> REPORT_SECTION_TYPE = new TypeReference<>() {
    };

    private final ReportMapper reportMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final JobMapper jobMapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public ReportServiceImpl(ReportMapper reportMapper,
                             StudentProfileMapper studentProfileMapper,
                             JobMapper jobMapper,
                             UserService userService,
                             ObjectMapper objectMapper) {
        this.reportMapper = reportMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.jobMapper = jobMapper;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ReportGenerateResponse generate(Long userId, ReportGenerateRequest request) {
        userService.loadActiveUser(userId);
        StudentProfileEntity profile = requireOwnedProfile(userId, request.getProfileId());
        JobEntity targetJob = resolveTargetJob(request.getTargetJobId());

        ReportEntity report = new ReportEntity();
        report.setUserId(userId);
        report.setProfileId(profile.getId());
        report.setTargetJobId(targetJob == null ? null : targetJob.getId());
        report.setTitle(targetJob == null ? "职业规划报告" : targetJob.getTitle() + " 职业规划报告");
        report.setContentJson(toJson(buildSections(profile, targetJob)));
        reportMapper.insert(report);
        return new ReportGenerateResponse(report.getId());
    }

    @Override
    public ReportResponse get(Long userId, Long reportId) {
        ReportEntity report = requireOwnedReport(userId, reportId);
        return toResponse(report);
    }

    @Override
    @Transactional
    public String polish(Long userId, Long reportId) {
        ReportEntity report = requireOwnedReport(userId, reportId);
        String polished = buildPolishedContent(readSections(report.getContentJson()));
        report.setPolishedContent(polished);
        reportMapper.update(report);
        return polished;
    }

    @Override
    public ResponseEntity<ByteArrayResource> export(Long userId, Long reportId, String format) {
        ReportEntity report = requireOwnedReport(userId, reportId);
        String normalizedFormat = normalizeFormat(format);
        String content = StringUtils.hasText(report.getPolishedContent())
                ? report.getPolishedContent()
                : buildPolishedContent(readSections(report.getContentJson()));
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(bytes);
        String filename = "report-" + report.getId() + ("pdf".equals(normalizedFormat) ? ".pdf" : ".docx");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Override
    public List<ReportListItemResponse> list(Long userId, Long profileId) {
        userService.loadActiveUser(userId);
        requireOwnedProfile(userId, profileId);
        return reportMapper.findByUserAndProfileId(userId, profileId).stream()
                .map(report -> new ReportListItemResponse(
                        report.getId(),
                        report.getProfileId(),
                        report.getTargetJobId(),
                        report.getTitle(),
                        report.getCreatedAt()
                ))
                .toList();
    }

    private StudentProfileEntity requireOwnedProfile(Long userId, Long profileId) {
        StudentProfileEntity profile = studentProfileMapper.findById(profileId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "profile not found");
        }
        if (!profile.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "profile does not belong to current user");
        }
        return profile;
    }

    private ReportEntity requireOwnedReport(Long userId, Long reportId) {
        userService.loadActiveUser(userId);
        ReportEntity report = reportMapper.findById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "report not found");
        }
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "report does not belong to current user");
        }
        return report;
    }

    private JobEntity resolveTargetJob(Long targetJobId) {
        if (targetJobId == null) {
            return null;
        }
        JobEntity job = jobMapper.findById(targetJobId);
        if (job == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "target job not found");
        }
        return job;
    }

    private List<ReportSectionResponse> buildSections(StudentProfileEntity profile, JobEntity targetJob) {
        List<String> profileSkills = readList(profile.getSkillsJson());
        List<String> jobSkills = targetJob == null ? Collections.emptyList() : readList(targetJob.getSkillsJson());
        List<String> missingSkills = jobSkills.stream()
                .filter(skill -> profileSkills.stream().noneMatch(existing -> existing.equalsIgnoreCase(skill)))
                .toList();

        List<ReportSectionResponse> sections = new ArrayList<>();
        sections.add(new ReportSectionResponse(
                "画像概览",
                "当前画像完整度为 " + profile.getCompletenessScore() + "，能力得分为 " + profile.getAbilityScore() + "，竞争力得分为 " + profile.getCompetitivenessScore() + "。"
        ));
        sections.add(new ReportSectionResponse(
                "能力与经历分析",
                "当前已识别技能：" + String.join("、", profileSkills) + "。教育背景概述：" + safe(profile.getEducationSummary()) + "。"
        ));
        sections.add(new ReportSectionResponse(
                "目标岗位洞察",
                targetJob == null
                        ? "当前尚未指定目标岗位，建议结合兴趣与市场需求进一步明确方向。"
                        : "目标岗位为 " + targetJob.getTitle() + "，岗位核心技能包括：" + String.join("、", jobSkills) + "。"
        ));
        sections.add(new ReportSectionResponse(
                "差距分析",
                missingSkills.isEmpty()
                        ? "当前画像与目标岗位要求较为接近，重点在于项目实践与求职表达。"
                        : "与目标岗位相比，建议优先补齐以下技能差距：" + String.join("、", missingSkills) + "。"
        ));
        sections.add(new ReportSectionResponse(
                "行动建议",
                "建议围绕画像补全、能力补齐、项目实践、简历优化和投递准备分阶段推进。"
        ));
        return sections;
    }

    private ReportResponse toResponse(ReportEntity report) {
        return new ReportResponse(
                report.getId(),
                report.getProfileId(),
                report.getTargetJobId(),
                report.getTitle(),
                readSections(report.getContentJson()),
                report.getPolishedContent(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }

    private List<String> readList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to parse json list");
        }
    }

    private List<ReportSectionResponse> readSections(String json) {
        try {
            List<ReportSectionResponse> sections = objectMapper.readValue(json, REPORT_SECTION_TYPE);
            return sections == null ? Collections.emptyList() : sections;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to parse report sections");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to serialize report");
        }
    }

    private String buildPolishedContent(List<ReportSectionResponse> sections) {
        StringBuilder builder = new StringBuilder();
        builder.append("职业规划报告").append(System.lineSeparator()).append(System.lineSeparator());
        for (ReportSectionResponse section : sections) {
            builder.append(section.title()).append(System.lineSeparator());
            builder.append(section.content()).append(System.lineSeparator()).append(System.lineSeparator());
        }
        builder.append("建议你结合阶段化计划持续推进，并定期复盘执行情况。");
        return builder.toString();
    }

    private String normalizeFormat(String format) {
        if (!StringUtils.hasText(format)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "format is required");
        }
        String normalized = format.trim().toLowerCase();
        if (!"pdf".equals(normalized) && !"word".equals(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "format must be pdf or word");
        }
        return normalized;
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "待补充";
    }
}
