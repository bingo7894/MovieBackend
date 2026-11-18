package com.nerflix.clone.service;

import com.nerflix.clone.dto.request.ReviewRequest;
import com.nerflix.clone.dto.response.CourseReviewsOverviewResponse;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.ReviewResponse;

public interface ReviewService {
    CourseReviewsOverviewResponse getCourseReviews(Long courseId, String email);

    ReviewResponse upsertReview(Long courseId, ReviewRequest request, String email);

    MessageResponse deleteReview(Long reviewId, String email);
}
