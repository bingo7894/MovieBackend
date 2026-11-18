package com.nerflix.clone.controller;

import com.nerflix.clone.dto.request.ReviewRequest;
import com.nerflix.clone.dto.response.CourseReviewsOverviewResponse;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.ReviewResponse;
import com.nerflix.clone.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Course Reviews", description = "Course ratings and reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Operation(summary = "Get course rating summary and learner reviews")
    @GetMapping("/courses/{courseId}/reviews")
    public ResponseEntity<CourseReviewsOverviewResponse> getCourseReviews(
            @PathVariable Long courseId,
            Authentication authentication) {
        return ResponseEntity.ok(reviewService.getCourseReviews(courseId, authentication.getName()));
    }

    @Operation(summary = "Create or update current learner's review for a course")
    @PostMapping("/courses/{courseId}/reviews")
    public ResponseEntity<ReviewResponse> upsertReview(
            @PathVariable Long courseId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(reviewService.upsertReview(courseId, request, authentication.getName()));
    }

    @Operation(summary = "Delete a review (owner or admin)")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<MessageResponse> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        return ResponseEntity.ok(reviewService.deleteReview(reviewId, authentication.getName()));
    }
}
