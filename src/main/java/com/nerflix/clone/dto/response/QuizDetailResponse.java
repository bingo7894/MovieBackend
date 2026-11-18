package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDetailResponse {
    private Long id;
    private String title;
    private String description;
    private Integer passingScore;
    private Integer timeLimitMinutes;
    private boolean shuffleQuestions;
    private boolean published;
    private Long episodeId;
    private List<QuizQuestionResponse> questions = new ArrayList<>();

    public QuizDetailResponse(
            Long id,
            String title,
            String description,
            Integer passingScore,
            boolean published,
            Long episodeId,
            List<QuizQuestionResponse> questions) {
        this(id, title, description, passingScore, 0, false, published, episodeId, questions);
    }
}
