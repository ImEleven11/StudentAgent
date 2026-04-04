package com.uestc.studentagent.backend.resource.service;

import com.uestc.studentagent.backend.resource.dto.CourseUpsertRequest;
import com.uestc.studentagent.backend.resource.dto.ResourceItemResponse;

import java.util.List;

public interface ResourceService {

    List<ResourceItemResponse> courses(List<String> skills, String level);

    List<ResourceItemResponse> books(List<String> skills);

    List<ResourceItemResponse> projects(List<String> skills, String difficulty);

    List<ResourceItemResponse> internships(String location, List<String> skills);

    List<ResourceItemResponse> competitions(String category, String timeRange);

    Long upsertCourse(Long userId, CourseUpsertRequest request);
}
