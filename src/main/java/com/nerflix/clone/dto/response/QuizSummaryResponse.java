package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSummaryResponse {
    private Long id;
    private String title;
    private String description;
    private Integer passingScore;
    private Integer timeLimitMinutes;
    private boolean shuffleQuestions;
    private boolean published;
    private int questionCount;
    private Integer bestScore;
    private Boolean passed;

    public QuizSummaryResponse(
            Long id,
            String title,
            String description,
            Integer passingScore,
            boolean published,
            int questionCount,
            Integer bestScore,
            Boolean passed) {
        this(id, title, description, passingScore, 0, false, published, questionCount, bestScore, passed);
    }
}
