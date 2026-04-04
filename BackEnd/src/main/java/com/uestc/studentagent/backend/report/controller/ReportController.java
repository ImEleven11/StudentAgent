package com.uestc.studentagent.backend.report.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.report.dto.ReportGenerateRequest;
import com.uestc.studentagent.backend.report.dto.ReportGenerateResponse;
import com.uestc.studentagent.backend.report.dto.ReportListItemResponse;
import com.uestc.studentagent.backend.report.dto.ReportResponse;
import com.uestc.studentagent.backend.report.service.ReportService;
import com.uestc.studentagent.backend.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@Tag(name = "Report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate a career planning report")
    public ApiResponse<ReportGenerateResponse> generate(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                        @Valid @RequestBody ReportGenerateRequest request) {
        return ApiResponse.success(reportService.generate(principal.getUserId(), request));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get report detail")
    public ApiResponse<ReportResponse> get(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                           @PathVariable Long reportId) {
        return ApiResponse.success(reportService.get(principal.getUserId(), reportId));
    }

    @PostMapping("/{reportId}/polish")
    @Operation(summary = "Polish report content")
    public ApiResponse<String> polish(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                      @PathVariable Long reportId) {
        return ApiResponse.success(reportService.polish(principal.getUserId(), reportId));
    }

    @GetMapping("/{reportId}/export")
    @Operation(summary = "Export a report file")
    public ResponseEntity<ByteArrayResource> export(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                    @PathVariable Long reportId,
                                                    @RequestParam String format) {
        return reportService.export(principal.getUserId(), reportId, format);
    }

    @GetMapping("/list")
    @Operation(summary = "List reports for a profile")
    public ApiResponse<List<ReportListItemResponse>> list(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                          @RequestParam Long profileId) {
        return ApiResponse.success(reportService.list(principal.getUserId(), profileId));
    }
}
