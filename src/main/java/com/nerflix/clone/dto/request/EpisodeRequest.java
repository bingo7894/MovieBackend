package com.nerflix.clone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EpisodeRequest {

    @NotBlank(message = "Episode title is required")
    private String title;

    @Size(max = 4000, message = "Description must not exceed 4000 characters")
    private String description;

    private Integer episodeNumber;

    private String videoUuid;

    private Integer duration;

    private Boolean freePreview;

    private Boolean published;
}
