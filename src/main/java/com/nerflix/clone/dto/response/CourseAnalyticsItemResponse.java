package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseAnalyticsItemResponse {
    private Long courseId;
    private String title;
    private String category;
    private String level;
    private boolean published;
    private int topicCount;
    private int episodeCount;
    private long enrolledCount;
    private int activeLearnerCount;
    private int completedLearnerCount;
    private int averageProgressPercent;
    private long certificatesIssued;
    private int quizAttemptCount;
    private int averageQuizScore;
    private double averageRating;
    private long reviewCount;
}
