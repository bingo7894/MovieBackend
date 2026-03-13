package com.nerflix.clone.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class VideoResponse {

    private Long id;
    private String title;
    private String description;
    private Integer year;
    private String rating;

    private Integer duration;
    private String src;
    private String poster;
    private boolean published;

    private List<String> categories;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isInWatchList;
    
}
