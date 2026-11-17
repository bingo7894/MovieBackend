package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressResponse {
    private Long episodeId;
    private Integer watchedSeconds;
    private boolean completed;
    private Instant completedAt;
}
