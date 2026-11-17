package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryResponse {
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
    private Instant createdAt;
    private Instant updatedAt;
}
