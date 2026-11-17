package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatsResponse {
    private long totalCourses;
    private long publishedCourses;
    private long totalTopics;
    private long totalEpisodes;
    private long totalDuration;
    private long totalQuizzes;
}
