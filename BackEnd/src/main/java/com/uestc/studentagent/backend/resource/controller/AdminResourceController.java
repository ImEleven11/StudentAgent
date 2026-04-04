package com.uestc.studentagent.backend.resource.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.resource.dto.CourseUpsertRequest;
import com.uestc.studentagent.backend.resource.service.ResourceService;
import com.uestc.studentagent.backend.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/resources")
@Tag(name = "Admin Resources")
public class AdminResourceController {

    private final ResourceService resourceService;

    public AdminResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/courses")
    @Operation(summary = "Create or update a course resource")
    public ApiResponse<Long> upsertCourse(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                          @Valid @RequestBody CourseUpsertRequest request) {
        return ApiResponse.success(resourceService.upsertCourse(principal.getUserId(), request));
    }
}
