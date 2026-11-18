package com.nerflix.clone.serviceimpl;

import com.nerflix.clone.dao.CourseReviewRepository;
import com.nerflix.clone.dto.request.ReviewRequest;
import com.nerflix.clone.dto.response.CourseReviewsOverviewResponse;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.ReviewResponse;
import com.nerflix.clone.entity.Course;
import com.nerflix.clone.entity.CourseReview;
import com.nerflix.clone.entity.User;
import com.nerflix.clone.enums.Role;
import com.nerflix.clone.exception.ResourceNotFoundException;
import com.nerflix.clone.service.ReviewService;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private CourseReviewRepository reviewRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private LmsMapper lmsMapper;

    @Override
    @Transactional(readOnly = true)
    public CourseReviewsOverviewResponse getCourseReviews(Long courseId, String email) {
        Course course = serviceUtils.getCourseByIdOrThrow(courseId);
        User user = serviceUtils.getUserByEmailOrThrow(email);
        if (!course.isPublished() && user.getRole() != Role.ADMIN) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        List<ReviewResponse> reviews = reviewRepository.findByCourseIdOrderByUpdatedAtDesc(courseId).stream()
                .map(r -> lmsMapper.toReviewResponse(r, user.getId()))
                .toList();

        ReviewResponse userReview = reviews.stream()
                .filter(ReviewResponse::isOwnReview)
                .findFirst()
                .orElse(null);

        double avg = reviews.stream()
                .mapToInt(ReviewResponse::getRating)
                .average()
                .orElse(0.0);
        double roundedAvg = Math.round(avg * 10.0) / 10.0;

        return new CourseReviewsOverviewResponse(roundedAvg, reviews.size(), userReview, reviews);
    }

    @Override
    @Transactional
    public ReviewResponse upsertReview(Long courseId, ReviewRequest request, String email) {
        Course course = serviceUtils.getCourseByIdOrThrow(courseId);
        if (!course.isPublished()) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        User user = serviceUtils.getUserByEmailOrThrow(email);

        CourseReview review = reviewRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseGet(() -> {
                    CourseReview r = new CourseReview();
                    r.setCourse(course);
                    r.setUser(user);
                    return r;
                });

        review.setRating(request.getRating());
        review.setComment(request.getComment() != null ? request.getComment().trim() : null);

        CourseReview saved = reviewRepository.save(review);
        return lmsMapper.toReviewResponse(saved, user.getId());
    }

    @Override
    @Transactional
    public MessageResponse deleteReview(Long reviewId, String email) {
        CourseReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        User user = serviceUtils.getUserByEmailOrThrow(email);
        boolean isOwner = review.getUser().getId().equals(user.getId());
        if (!isOwner && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You cannot delete this review");
        }
        reviewRepository.delete(review);
        return new MessageResponse("Review deleted successfully.");
    }
}
