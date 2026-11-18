package com.nerflix.clone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DiscussionRequest {

    @NotBlank(message = "Question title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Question details are required")
    @Size(max = 4000, message = "Content must not exceed 4000 characters")
    private String content;

    private Integer videoTimestamp = 0;
}
