package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponse {
    private Long id;
    private String questionText;
    private String questionType;
    private String explanation;
    private Integer sortOrder;
    private List<QuizOptionResponse> options = new ArrayList<>();
}
