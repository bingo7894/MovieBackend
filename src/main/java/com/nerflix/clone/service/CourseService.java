package com.nerflix.clone.service;

import com.nerflix.clone.dto.request.CourseRequest;
import com.nerflix.clone.dto.response.*;

import java.util.List;

public interface CourseService {
    CourseSummaryResponse createCourse(CourseRequest request);

    PageResponse<CourseSummaryResponse> getAllAdminCourses(int page, int size, String search);

    CourseDetailResponse getCourseDetailForAdmin(Long courseId);

    CourseSummaryResponse updateCourse(Long id, CourseRequest request);

    MessageResponse deleteCourse(Long id);

    MessageResponse toggleCoursePublish(Long id, boolean published);

    CourseStatsResponse getAdminStats();

    PageResponse<CourseSummaryResponse> getPublishedCourses(int page, int size, String search, String category, String email);

    List<CourseSummaryResponse> getFeaturedCourses(String email);

    List<CourseSummaryResponse> getInProgressCourses(String email);

    CourseDetailResponse getCourseDetailForLearner(Long courseId, String email);
}
