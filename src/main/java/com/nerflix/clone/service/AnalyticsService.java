package com.nerflix.clone.service;

import com.nerflix.clone.dto.response.AdminAnalyticsOverviewResponse;
import com.nerflix.clone.dto.response.CourseLearnerProgressResponse;

import java.util.List;

public interface AnalyticsService {
    AdminAnalyticsOverviewResponse getOverview();

    List<CourseLearnerProgressResponse> getCourseLearners(Long courseId);

    byte[] exportCourseLearnersCsv(Long courseId);
}
