package com.nerflix.clone.dao;

import com.nerflix.clone.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    Optional<CourseReview> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseReview> findByCourseIdOrderByUpdatedAtDesc(Long courseId);

    @Query("SELECT r.course.id, COALESCE(AVG(r.rating), 0), COUNT(r) " +
            "FROM CourseReview r WHERE r.course.id IN :courseIds GROUP BY r.course.id")
    List<Object[]> getReviewStatsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM CourseReview r WHERE r.course.id = :courseId")
    Double getAverageRatingForCourse(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);

    void deleteByCourseId(Long courseId);

    void deleteByUserId(Long userId);
}
