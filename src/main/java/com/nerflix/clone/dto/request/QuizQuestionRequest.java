package com.nerflix.clone.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuizQuestionRequest {

    @NotBlank(message = "Question text is required")
    @Size(max = 2000)
    private String questionText;

    private String questionType;

    @Size(max = 2000)
    private String explanation;

    private Integer sortOrder;

    @Valid
    private List<QuizOptionRequest> options = new ArrayList<>();
}
