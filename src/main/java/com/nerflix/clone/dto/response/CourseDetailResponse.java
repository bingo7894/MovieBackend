package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String thumbnail;
    private String thumbnailUuid;
    private String category;
    private String level;
    private boolean published;
    private int topicCount;
    private int episodeCount;
    private long totalDuration;
    private boolean enrolled;
    private int completedEpisodes;
    private int progressPercent;
    private double averageRating;
    private long reviewCount;
    private long enrollmentCount;
    private boolean allQuizzesPassed;
    private boolean certificateEligible;
    private String certificateCode;
    private Instant createdAt;
    private Instant updatedAt;
    private List<TopicResponse> topics = new ArrayList<>();

    public CourseDetailResponse(
            Long id,
            String title,
            String description,
            String thumbnail,
            String thumbnailUuid,
            String category,
            String level,
            boolean published,
            int topicCount,
            int episodeCount,
            long totalDuration,
            boolean enrolled,
            int completedEpisodes,
            int progressPercent,
            Instant createdAt,
            Instant updatedAt,
            List<TopicResponse> topics) {
        this(id, title, description, thumbnail, thumbnailUuid, category, level, published,
                topicCount, episodeCount, totalDuration, enrolled, completedEpisodes, progressPercent,
                0.0, 0L, 0L, false, false, null, createdAt, updatedAt, topics);
    }
}
