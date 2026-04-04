package com.uestc.studentagent.backend.profile.service;

import com.uestc.studentagent.backend.profile.dto.ProfileIdentifierResponse;
import com.uestc.studentagent.backend.profile.dto.ProfileInputRequest;
import com.uestc.studentagent.backend.profile.dto.ResumeUploadResponse;
import com.uestc.studentagent.backend.profile.dto.StudentProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentProfileService {

    ResumeUploadResponse uploadResume(Long userId, MultipartFile file);

    ProfileIdentifierResponse manualInput(Long userId, ProfileInputRequest request);

    StudentProfileResponse getProfile(Long userId, Long profileId);

    StudentProfileResponse updateProfile(Long userId, Long profileId, ProfileInputRequest request);

    List<String> getMissingFields(Long userId, Long profileId);

    StudentProfileResponse completeProfile(Long userId, Long profileId, ProfileInputRequest request);
}
