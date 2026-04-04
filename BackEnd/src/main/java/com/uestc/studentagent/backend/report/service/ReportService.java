package com.uestc.studentagent.backend.report.service;

import com.uestc.studentagent.backend.report.dto.ReportGenerateRequest;
import com.uestc.studentagent.backend.report.dto.ReportGenerateResponse;
import com.uestc.studentagent.backend.report.dto.ReportListItemResponse;
import com.uestc.studentagent.backend.report.dto.ReportResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReportService {

    ReportGenerateResponse generate(Long userId, ReportGenerateRequest request);

    ReportResponse get(Long userId, Long reportId);

    String polish(Long userId, Long reportId);

    ResponseEntity<ByteArrayResource> export(Long userId, Long reportId, String format);

    List<ReportListItemResponse> list(Long userId, Long profileId);
}
