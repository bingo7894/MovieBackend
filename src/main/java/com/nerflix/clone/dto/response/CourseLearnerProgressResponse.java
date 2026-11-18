package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseLearnerProgressResponse {
    private Long userId;
    private String fullName;
    private String email;
    private boolean enrolled;
    private Instant enrolledAt;
    private int completedEpisodes;
    private int totalEpisodes;
    private int progressPercent;
    private long totalWatchedSeconds;
    private int quizAttemptsCount;
    private int quizzesPassedCount;
    private int totalPublishedQuizzes;
    private Integer averageQuizScore;
    private String certificateCode;
    private Instant lastActivityAt;
}
