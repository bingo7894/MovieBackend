package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponse {
    private Long id;
    private String title;
    private String description;
    private Integer sortOrder;
    private Long courseId;
    private long totalDuration;
    private int completedEpisodes;
    private List<EpisodeResponse> episodes = new ArrayList<>();
}
