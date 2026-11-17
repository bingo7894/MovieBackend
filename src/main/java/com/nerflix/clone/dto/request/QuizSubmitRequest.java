package com.nerflix.clone.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class QuizSubmitRequest {

    @NotNull(message = "Answers map is required")
    private Map<Long, List<Long>> answers = new HashMap<>();
}
