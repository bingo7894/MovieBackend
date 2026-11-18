package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsOverviewResponse {
    private long totalLearners;
    private long totalEnrollments;
    private long totalCertificatesIssued;
    private long totalQuizAttempts;
    private int averageQuizPassRate;
    private List<CourseAnalyticsItemResponse> courses = new ArrayList<>();
}
