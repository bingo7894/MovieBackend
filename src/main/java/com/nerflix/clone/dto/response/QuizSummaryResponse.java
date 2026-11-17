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
    private boolean published;
    private int questionCount;
    private Integer bestScore;
    private Boolean passed;
}
