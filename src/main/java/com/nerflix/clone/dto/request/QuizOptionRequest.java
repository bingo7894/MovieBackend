package com.nerflix.clone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuizOptionRequest {

    @NotBlank(message = "Option text is required")
    @Size(max = 1000)
    private String optionText;

    private boolean correct;
}
