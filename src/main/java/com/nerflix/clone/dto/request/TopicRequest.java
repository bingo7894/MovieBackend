package com.nerflix.clone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicRequest {

    @NotBlank(message = "Topic title is required")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Integer sortOrder;
}
