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

import java.io.ByteArrayOutputStream;
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
        report.setTitle(targetJob == null ? "Career Planning Report" : targetJob.getTitle() + " Career Planning Report");
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
        List<ReportSectionResponse> sections = readSections(report.getContentJson());
        byte[] bytes = "pdf".equals(normalizedFormat)
                ? buildPdfBytes(report, sections)
                : buildWordBytes(report, sections);
        ByteArrayResource resource = new ByteArrayResource(bytes);
        String filename = "report-" + report.getId() + ("pdf".equals(normalizedFormat) ? ".pdf" : ".doc");
        MediaType mediaType = "pdf".equals(normalizedFormat)
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/msword");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentLength(bytes.length)
                .contentType(mediaType)
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
                "Profile Overview",
                "Current profile completeness is " + profile.getCompletenessScore()
                        + ", ability score is " + profile.getAbilityScore()
                        + ", and competitiveness score is " + profile.getCompetitivenessScore() + "."
        ));
        sections.add(new ReportSectionResponse(
                "Skills and Experience",
                "Recognized skills: " + joinValues(profileSkills) + ". Education summary: " + safe(profile.getEducationSummary()) + "."
        ));
        sections.add(new ReportSectionResponse(
                "Target Role Insight",
                targetJob == null
                        ? "No target role is selected yet. Refine the direction with your interests and current market demand."
                        : "Target role: " + targetJob.getTitle() + ". Core skills include: " + joinValues(jobSkills) + "."
        ));
        sections.add(new ReportSectionResponse(
                "Gap Analysis",
                missingSkills.isEmpty()
                        ? "The current profile is already close to the target role. Focus on project practice and interview communication."
                        : "Compared with the target role, prioritize these missing skills: " + joinValues(missingSkills) + "."
        ));
        sections.add(new ReportSectionResponse(
                "Action Plan",
                "Move forward in phases: complete the profile, close skill gaps, build project evidence, improve the resume, and prepare for applications."
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
        builder.append("Career Planning Report").append(System.lineSeparator()).append(System.lineSeparator());
        for (ReportSectionResponse section : sections) {
            builder.append(section.title()).append(System.lineSeparator());
            builder.append(section.content()).append(System.lineSeparator()).append(System.lineSeparator());
        }
        builder.append("Keep advancing with the staged plan and review progress regularly.");
        return builder.toString();
    }

    private byte[] buildWordBytes(ReportEntity report, List<ReportSectionResponse> sections) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=\"UTF-8\"></head><body>");
        html.append("<h1>").append(escapeHtml(report.getTitle())).append("</h1>");
        for (ReportSectionResponse section : sections) {
            html.append("<h2>").append(escapeHtml(section.title())).append("</h2>");
            html.append("<p>").append(escapeHtml(section.content())).append("</p>");
        }
        html.append("<p>").append(escapeHtml("Keep advancing with the staged plan and review progress regularly.")).append("</p>");
        html.append("</body></html>");
        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildPdfBytes(ReportEntity report, List<ReportSectionResponse> sections) {
        List<String> lines = new ArrayList<>();
        lines.add(sanitizePdfText(report.getTitle()));
        lines.add("");
        for (ReportSectionResponse section : sections) {
            lines.add(sanitizePdfText(section.title()));
            lines.addAll(wrapPdfLine(sanitizePdfText(section.content()), 88));
            lines.add("");
        }
        lines.add("Keep advancing with the staged plan and review progress regularly.");

        StringBuilder content = new StringBuilder();
        content.append("BT\n");
        content.append("/F1 12 Tf\n");
        content.append("50 780 Td\n");

        boolean firstLine = true;
        for (String line : lines.stream().limit(40).toList()) {
            if (!firstLine) {
                content.append("0 -16 Td\n");
            }
            content.append("(").append(escapePdfText(line)).append(") Tj\n");
            firstLine = false;
        }
        content.append("ET\n");

        byte[] contentBytes = content.toString().getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        writeAscii(output, "%PDF-1.4\n");

        offsets.add(output.size());
        writeAscii(output, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets.add(output.size());
        writeAscii(output, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        offsets.add(output.size());
        writeAscii(output, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

        offsets.add(output.size());
        writeAscii(output, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        offsets.add(output.size());
        writeAscii(output, "5 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n");
        output.writeBytes(contentBytes);
        writeAscii(output, "endstream\nendobj\n");

        int xrefOffset = output.size();
        writeAscii(output, "xref\n0 6\n");
        writeAscii(output, "0000000000 65535 f \n");
        for (Integer offset : offsets) {
            writeAscii(output, String.format("%010d 00000 n \n", offset));
        }
        writeAscii(output, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF");
        return output.toByteArray();
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
        return StringUtils.hasText(value) ? value : "Not provided";
    }

    private String joinValues(List<String> values) {
        return values == null || values.isEmpty() ? "None" : String.join(", ", values);
    }

    private List<String> wrapPdfLine(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return List.of(value);
        }

        List<String> lines = new ArrayList<>();
        int start = 0;
        while (start < value.length()) {
            int end = Math.min(start + maxLength, value.length());
            lines.add(value.substring(start, end));
            start = end;
        }
        return lines;
    }

    private String sanitizePdfText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (char character : value.toCharArray()) {
            builder.append(character <= 0x7F ? character : '?');
        }
        return builder.toString();
    }

    private String escapePdfText(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void writeAscii(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }
}
