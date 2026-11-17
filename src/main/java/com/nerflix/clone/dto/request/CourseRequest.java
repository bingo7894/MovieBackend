package com.nerflix.clone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequest {

    @NotBlank(message = "Course title is required")
    private String title;

    @Size(max = 4000, message = "Description must not exceed 4000 characters")
    private String description;

    private String thumbnailUuid;

    private String category;

    private String level;

    private Boolean published;
}
