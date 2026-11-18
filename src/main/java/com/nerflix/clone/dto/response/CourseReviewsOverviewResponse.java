package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewsOverviewResponse {
    private double averageRating;
    private long reviewCount;
    private ReviewResponse userReview;
    private List<ReviewResponse> reviews = new ArrayList<>();
}
