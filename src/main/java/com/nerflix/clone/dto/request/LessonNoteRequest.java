package com.nerflix.clone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LessonNoteRequest {

    private Integer timestampSeconds;

    @NotBlank(message = "Note content is required")
    @Size(max = 4000)
    private String content;
}
