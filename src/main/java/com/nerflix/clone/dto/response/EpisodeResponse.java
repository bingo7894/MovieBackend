package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeResponse {
    private Long id;
    private String title;
    private String description;
    private Integer episodeNumber;
    private String videoUrl;
    private String videoUuid;
    private Integer duration;
    private boolean freePreview;
    private boolean published;
    private Long topicId;
    private Integer watchedSeconds;
    private boolean completed;
    private List<AttachmentResponse> attachments = new ArrayList<>();
    private List<QuizSummaryResponse> quizzes = new ArrayList<>();
}
