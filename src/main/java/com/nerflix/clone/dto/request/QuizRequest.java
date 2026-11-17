package com.nerflix.clone.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuizRequest {

    @NotBlank(message = "Quiz title is required")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Integer passingScore;

    private Boolean published;

    @Valid
    private List<QuizQuestionRequest> questions = new ArrayList<>();
}
