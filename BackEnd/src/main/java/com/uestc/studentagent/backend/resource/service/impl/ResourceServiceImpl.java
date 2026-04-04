package com.uestc.studentagent.backend.resource.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.resource.dto.CourseUpsertRequest;
import com.uestc.studentagent.backend.resource.dto.ResourceItemResponse;
import com.uestc.studentagent.backend.resource.entity.ResourceEntity;
import com.uestc.studentagent.backend.resource.entity.ResourceType;
import com.uestc.studentagent.backend.resource.mapper.ResourceMapper;
import com.uestc.studentagent.backend.resource.service.ResourceService;
import com.uestc.studentagent.backend.user.entity.UserRole;
import com.uestc.studentagent.backend.user.entity.UserEntity;
import com.uestc.studentagent.backend.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final ResourceMapper resourceMapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public ResourceServiceImpl(ResourceMapper resourceMapper, UserService userService, ObjectMapper objectMapper) {
        this.resourceMapper = resourceMapper;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ResourceItemResponse> courses(List<String> skills, String level) {
        return filterBySkills(resourceMapper.search(ResourceType.COURSE.name(), normalize(level), null, null, null, null), skills);
    }

    @Override
    public List<ResourceItemResponse> books(List<String> skills) {
        return filterBySkills(resourceMapper.search(ResourceType.BOOK.name(), null, null, null, null, null), skills);
    }

    @Override
    public List<ResourceItemResponse> projects(List<String> skills, String difficulty) {
        return filterBySkills(resourceMapper.search(ResourceType.PROJECT.name(), null, normalize(difficulty), null, null, null), skills);
    }

    @Override
    public List<ResourceItemResponse> internships(String location, List<String> skills) {
        return filterBySkills(resourceMapper.search(ResourceType.INTERNSHIP.name(), null, null, normalize(location), null, null), skills);
    }

    @Override
    public List<ResourceItemResponse> competitions(String category, String timeRange) {
        return resourceMapper.search(ResourceType.COMPETITION.name(), null, null, null, normalize(category), normalize(timeRange)).stream()
                .map(resource -> ResourceItemResponse.from(resource, readSkills(resource.getSkillsJson())))
                .toList();
    }

    @Override
    @Transactional
    public Long upsertCourse(Long userId, CourseUpsertRequest request) {
        UserEntity user = userService.loadActiveUser(userId);
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "admin permission is required");
        }

        ResourceEntity entity = request.getResourceId() == null
                ? new ResourceEntity()
                : requireResource(request.getResourceId());

        entity.setResourceType(ResourceType.COURSE);
        entity.setTitle(request.getTitle().trim());
        entity.setDescription(normalize(request.getDescription()));
        entity.setLink(normalize(request.getLink()));
        entity.setSkillsJson(toJson(normalizeSkills(request.getSkills())));
        entity.setLevelTag(normalize(request.getLevel()));
        entity.setDurationText(normalize(request.getDuration()));

        if (entity.getId() == null) {
            resourceMapper.insert(entity);
        } else {
            resourceMapper.update(entity);
        }
        return entity.getId();
    }

    private List<ResourceItemResponse> filterBySkills(List<ResourceEntity> resources, List<String> expectedSkills) {
        List<String> normalizedExpectedSkills = normalizeSkills(expectedSkills);
        return resources.stream()
                .filter(resource -> normalizedExpectedSkills.isEmpty() || hasSkillOverlap(readSkills(resource.getSkillsJson()), normalizedExpectedSkills))
                .map(resource -> ResourceItemResponse.from(resource, readSkills(resource.getSkillsJson())))
                .toList();
    }

    private boolean hasSkillOverlap(List<String> resourceSkills, List<String> expectedSkills) {
        return resourceSkills.stream().anyMatch(resourceSkill ->
                expectedSkills.stream().anyMatch(expected -> expected.equalsIgnoreCase(resourceSkill)));
    }

    private ResourceEntity requireResource(Long resourceId) {
        ResourceEntity entity = resourceMapper.findById(resourceId);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "resource not found");
        }
        return entity;
    }

    private List<String> readSkills(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to parse resource skills");
        }
    }

    private String toJson(List<String> skills) {
        try {
            return objectMapper.writeValueAsString(skills == null ? Collections.emptyList() : skills);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to serialize resource skills");
        }
    }

    private List<String> normalizeSkills(List<String> skills) {
        if (CollectionUtils.isEmpty(skills)) {
            return Collections.emptyList();
        }
        return skills.stream()
                .filter(StringUtils::hasText)
                .map(skill -> skill.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
