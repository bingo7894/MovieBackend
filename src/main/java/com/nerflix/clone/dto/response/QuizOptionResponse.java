package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionResponse {
    private Long id;
    private String optionText;
    private Boolean correct;
}
