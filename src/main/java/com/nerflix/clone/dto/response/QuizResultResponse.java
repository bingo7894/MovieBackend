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
public class QuizResultResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Integer scorePercent;
    private Integer correctCount;
    private Integer totalQuestions;
    private Integer passingScore;
    private boolean passed;
    private Instant submittedAt;
    private List<QuestionResultItem> questionResults = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResultItem {
        private Long questionId;
        private String questionText;
        private List<Long> selectedOptionIds;
        private List<Long> correctOptionIds;
        private boolean correct;
        private String explanation;
    }
}
