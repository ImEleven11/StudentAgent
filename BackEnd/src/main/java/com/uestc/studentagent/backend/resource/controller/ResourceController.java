package com.uestc.studentagent.backend.resource.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.resource.dto.ResourceItemResponse;
import com.uestc.studentagent.backend.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@Tag(name = "Resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/courses")
    @Operation(summary = "Get course recommendations")
    public ApiResponse<List<ResourceItemResponse>> courses(@RequestParam(required = false) List<String> skills,
                                                           @RequestParam(required = false) String level) {
        return ApiResponse.success(resourceService.courses(skills, level));
    }

    @GetMapping("/books")
    @Operation(summary = "Get book recommendations")
    public ApiResponse<List<ResourceItemResponse>> books(@RequestParam(required = false) List<String> skills) {
        return ApiResponse.success(resourceService.books(skills));
    }

    @GetMapping("/projects")
    @Operation(summary = "Get project recommendations")
    public ApiResponse<List<ResourceItemResponse>> projects(@RequestParam(required = false) List<String> skills,
                                                            @RequestParam(required = false) String difficulty) {
        return ApiResponse.success(resourceService.projects(skills, difficulty));
    }

    @GetMapping("/internships")
    @Operation(summary = "Get internship recommendations")
    public ApiResponse<List<ResourceItemResponse>> internships(@RequestParam(required = false) String location,
                                                               @RequestParam(required = false) List<String> skills) {
        return ApiResponse.success(resourceService.internships(location, skills));
    }

    @GetMapping("/competitions")
    @Operation(summary = "Get competition recommendations")
    public ApiResponse<List<ResourceItemResponse>> competitions(@RequestParam(required = false) String category,
                                                                @RequestParam(required = false) String timeRange) {
        return ApiResponse.success(resourceService.competitions(category, timeRange));
    }
}
