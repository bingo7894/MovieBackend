package com.nerflix.clone.dao;

import com.nerflix.clone.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    @Query("SELECT e FROM Enrollment e JOIN e.course c WHERE e.user.id = :userId AND c.published = true ORDER BY e.enrolledAt DESC")
    Page<Enrollment> findPublishedByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT e FROM Enrollment e JOIN e.course c WHERE e.user.id = :userId AND c.published = true AND (" +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.enrolledAt DESC")
    Page<Enrollment> searchPublishedByUserId(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

    @Query("SELECT e.course.id FROM Enrollment e WHERE e.user.id = :userId AND e.course.id IN :courseIds")
    Set<Long> findEnrolledCourseIds(@Param("userId") Long userId, @Param("courseIds") List<Long> courseIds);

    void deleteByCourseId(Long courseId);

    void deleteByUserId(Long userId);
}
