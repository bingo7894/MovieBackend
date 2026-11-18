package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long courseId;
    private Long authorId;
    private String authorName;
    private Integer rating;
    private String comment;
    private boolean ownReview;
    private Instant createdAt;
    private Instant updatedAt;
}
