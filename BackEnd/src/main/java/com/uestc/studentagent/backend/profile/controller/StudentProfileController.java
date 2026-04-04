package com.uestc.studentagent.backend.profile.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.profile.dto.ProfileIdentifierResponse;
import com.uestc.studentagent.backend.profile.dto.ProfileInputRequest;
import com.uestc.studentagent.backend.profile.dto.ResumeUploadResponse;
import com.uestc.studentagent.backend.profile.dto.StudentProfileResponse;
import com.uestc.studentagent.backend.profile.service.StudentProfileService;
import com.uestc.studentagent.backend.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Student Profile")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    public StudentProfileController(StudentProfileService studentProfileService) {
        this.studentProfileService = studentProfileService;
    }

    @PostMapping("/upload-resume")
    @Operation(summary = "Upload a resume file and create or update the current user's profile")
    public ApiResponse<ResumeUploadResponse> uploadResume(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                          @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(studentProfileService.uploadResume(principal.getUserId(), file));
    }

    @PostMapping("/manual-input")
    @Operation(summary = "Create or update the current user's profile from manual form input")
    public ApiResponse<ProfileIdentifierResponse> manualInput(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                              @Valid @RequestBody ProfileInputRequest request) {
        return ApiResponse.success(studentProfileService.manualInput(principal.getUserId(), request));
    }

    @GetMapping("/{profileId}")
    @Operation(summary = "Get a student profile by id")
    public ApiResponse<StudentProfileResponse> getProfile(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                          @PathVariable Long profileId) {
        return ApiResponse.success(studentProfileService.getProfile(principal.getUserId(), profileId));
    }

    @PutMapping("/{profileId}")
    @Operation(summary = "Update a student profile by id")
    public ApiResponse<StudentProfileResponse> updateProfile(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                             @PathVariable Long profileId,
                                                             @Valid @RequestBody ProfileInputRequest request) {
        return ApiResponse.success(studentProfileService.updateProfile(principal.getUserId(), profileId, request));
    }

    @GetMapping("/{profileId}/missing-fields")
    @Operation(summary = "Get missing fields for the current user's profile")
    public ApiResponse<List<String>> getMissingFields(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                      @PathVariable Long profileId) {
        return ApiResponse.success(studentProfileService.getMissingFields(principal.getUserId(), profileId));
    }

    @PostMapping("/{profileId}/complete")
    @Operation(summary = "Complete missing profile fields in batch")
    public ApiResponse<StudentProfileResponse> completeProfile(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                               @PathVariable Long profileId,
                                                               @Valid @RequestBody ProfileInputRequest request) {
        return ApiResponse.success(studentProfileService.completeProfile(principal.getUserId(), profileId, request));
    }
}
