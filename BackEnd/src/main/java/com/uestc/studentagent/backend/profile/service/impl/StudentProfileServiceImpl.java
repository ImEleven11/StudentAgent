package com.uestc.studentagent.backend.profile.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.common.storage.FileStorageService;
import com.uestc.studentagent.backend.common.storage.StoredFile;
import com.uestc.studentagent.backend.profile.dto.ProfileIdentifierResponse;
import com.uestc.studentagent.backend.profile.dto.ProfileInputRequest;
import com.uestc.studentagent.backend.profile.dto.ResumeUploadResponse;
import com.uestc.studentagent.backend.profile.dto.StudentProfileResponse;
import com.uestc.studentagent.backend.profile.entity.ProfileParseStatus;
import com.uestc.studentagent.backend.profile.entity.StudentProfileEntity;
import com.uestc.studentagent.backend.profile.integration.ProfileMlGateway;
import com.uestc.studentagent.backend.profile.mapper.StudentProfileMapper;
import com.uestc.studentagent.backend.profile.service.StudentProfileService;
import com.uestc.studentagent.backend.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final StudentProfileMapper studentProfileMapper;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final ProfileMlGateway profileMlGateway;
    private final ObjectMapper objectMapper;

    public StudentProfileServiceImpl(StudentProfileMapper studentProfileMapper,
                                     UserService userService,
                                     FileStorageService fileStorageService,
                                     ProfileMlGateway profileMlGateway,
                                     ObjectMapper objectMapper) {
        this.studentProfileMapper = studentProfileMapper;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.profileMlGateway = profileMlGateway;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ResumeUploadResponse uploadResume(Long userId, MultipartFile file) {
        userService.loadActiveUser(userId);
        StoredFile storedFile = fileStorageService.storeResume(userId, file);

        StudentProfileEntity profile = getOrCreateProfile(userId);
        profile.setResumeFileName(storedFile.originalFileName());
        profile.setResumeStoragePath(storedFile.storagePath());
        profile.setResumeContentType(storedFile.contentType());
        profile.setParseStatus(ProfileParseStatus.PENDING);
        profile.setParseProgress(15);
        refreshScores(profile);
        saveProfile(profile);

        profileMlGateway.notifyResumeUploaded(profile);
        return new ResumeUploadResponse(profile.getId(), profile.getParseProgress());
    }

    @Override
    @Transactional
    public ProfileIdentifierResponse manualInput(Long userId, ProfileInputRequest request) {
        userService.loadActiveUser(userId);
        StudentProfileEntity profile = getOrCreateProfile(userId);
        mergeProfileInput(profile, request);

        if (profile.getResumeFileName() == null) {
            profile.setParseStatus(ProfileParseStatus.COMPLETED);
            profile.setParseProgress(100);
        }

        refreshScores(profile);
        saveProfile(profile);
        return new ProfileIdentifierResponse(profile.getId());
    }

    @Override
    public StudentProfileResponse getProfile(Long userId, Long profileId) {
        StudentProfileEntity profile = requireOwnedProfile(userId, profileId);
        return toResponse(profile);
    }

    @Override
    @Transactional
    public StudentProfileResponse updateProfile(Long userId, Long profileId, ProfileInputRequest request) {
        StudentProfileEntity profile = requireOwnedProfile(userId, profileId);
        mergeProfileInput(profile, request);
        refreshScores(profile);
        saveProfile(profile);
        return toResponse(profile);
    }

    @Override
    public List<String> getMissingFields(Long userId, Long profileId) {
        StudentProfileEntity profile = requireOwnedProfile(userId, profileId);
        return calculateMissingFields(profile);
    }

    @Override
    @Transactional
    public StudentProfileResponse completeProfile(Long userId, Long profileId, ProfileInputRequest request) {
        StudentProfileEntity profile = requireOwnedProfile(userId, profileId);
        mergeProfileInput(profile, request);
        if (calculateMissingFields(profile).isEmpty()) {
            profile.setParseStatus(ProfileParseStatus.COMPLETED);
            profile.setParseProgress(100);
        } else if (profile.getParseProgress() == null || profile.getParseProgress() < 60) {
            profile.setParseProgress(60);
        }
        refreshScores(profile);
        saveProfile(profile);
        return toResponse(profile);
    }

    private StudentProfileEntity getOrCreateProfile(Long userId) {
        StudentProfileEntity existing = studentProfileMapper.findByUserId(userId);
        if (existing != null) {
            return existing;
        }

        StudentProfileEntity profile = new StudentProfileEntity();
        profile.setUserId(userId);
        profile.setSkillsJson(toJson(Collections.emptyList()));
        profile.setExperiencesJson(toJson(Collections.emptyList()));
        profile.setCertificatesJson(toJson(Collections.emptyList()));
        profile.setAbilityScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        profile.setCompetitivenessScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        profile.setCompletenessScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        profile.setParseStatus(ProfileParseStatus.PENDING);
        profile.setParseProgress(0);
        studentProfileMapper.insert(profile);
        return profile;
    }

    private StudentProfileEntity requireOwnedProfile(Long userId, Long profileId) {
        userService.loadActiveUser(userId);
        StudentProfileEntity profile = studentProfileMapper.findById(profileId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "profile not found");
        }
        if (!profile.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "profile does not belong to current user");
        }
        return profile;
    }

    private void mergeProfileInput(StudentProfileEntity profile, ProfileInputRequest request) {
        if (request.getEducationSummary() != null) {
            profile.setEducationSummary(normalizeNullable(request.getEducationSummary()));
        }
        if (request.getSkills() != null) {
            profile.setSkillsJson(toJson(normalizeList(request.getSkills())));
        }
        if (request.getExperiences() != null) {
            profile.setExperiencesJson(toJson(normalizeList(request.getExperiences())));
        }
        if (request.getCertificates() != null) {
            profile.setCertificatesJson(toJson(normalizeList(request.getCertificates())));
        }
        if (request.getInnovationSummary() != null) {
            profile.setInnovationSummary(normalizeNullable(request.getInnovationSummary()));
        }
        if (request.getInterestSummary() != null) {
            profile.setInterestSummary(normalizeNullable(request.getInterestSummary()));
        }
        if (request.getPersonalitySummary() != null) {
            profile.setPersonalitySummary(normalizeNullable(request.getPersonalitySummary()));
        }
    }

    private void refreshScores(StudentProfileEntity profile) {
        List<String> skills = readList(profile.getSkillsJson());
        List<String> experiences = readList(profile.getExperiencesJson());
        List<String> certificates = readList(profile.getCertificatesJson());
        List<String> missingFields = calculateMissingFields(profile);

        int totalCoreFields = 6;
        BigDecimal completeness = percentage(totalCoreFields - missingFields.size(), totalCoreFields);
        BigDecimal ability = BigDecimal.valueOf(
                        Math.min(100, 20
                                + skills.size() * 8
                                + experiences.size() * 12
                                + certificates.size() * 6
                                + (StringUtils.hasText(profile.getInnovationSummary()) ? 10 : 0)
                                + (StringUtils.hasText(profile.getEducationSummary()) ? 12 : 0)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal competitiveness = ability.multiply(BigDecimal.valueOf(0.7))
                .add(completeness.multiply(BigDecimal.valueOf(0.3)))
                .setScale(2, RoundingMode.HALF_UP);

        profile.setAbilityScore(ability);
        profile.setCompletenessScore(completeness);
        profile.setCompetitivenessScore(competitiveness);

        if (profile.getParseStatus() == null) {
            profile.setParseStatus(ProfileParseStatus.PENDING);
        }
        if (profile.getParseProgress() == null) {
            profile.setParseProgress(0);
        }
    }

    private List<String> calculateMissingFields(StudentProfileEntity profile) {
        List<String> missingFields = new ArrayList<>();
        if (!StringUtils.hasText(profile.getEducationSummary())) {
            missingFields.add("educationSummary");
        }
        if (readList(profile.getSkillsJson()).isEmpty()) {
            missingFields.add("skills");
        }
        if (readList(profile.getExperiencesJson()).isEmpty()) {
            missingFields.add("experiences");
        }
        if (readList(profile.getCertificatesJson()).isEmpty()) {
            missingFields.add("certificates");
        }
        if (!StringUtils.hasText(profile.getInterestSummary())) {
            missingFields.add("interestSummary");
        }
        if (!StringUtils.hasText(profile.getPersonalitySummary())) {
            missingFields.add("personalitySummary");
        }
        return missingFields;
    }

    private StudentProfileResponse toResponse(StudentProfileEntity profile) {
        return StudentProfileResponse.from(
                profile,
                readList(profile.getSkillsJson()),
                readList(profile.getExperiencesJson()),
                readList(profile.getCertificatesJson()),
                calculateMissingFields(profile)
        );
    }

    private void saveProfile(StudentProfileEntity profile) {
        if (profile.getId() == null) {
            studentProfileMapper.insert(profile);
        } else {
            studentProfileMapper.update(profile);
        }
    }

    private BigDecimal percentage(int completed, int total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private List<String> readList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to parse stored profile data");
        }
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to serialize profile data");
        }
    }

    private List<String> normalizeList(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
