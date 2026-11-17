package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptSummaryResponse {
    private Long id;
    private Integer scorePercent;
    private Integer correctCount;
    private Integer totalQuestions;
    private boolean passed;
    private Instant submittedAt;
}
