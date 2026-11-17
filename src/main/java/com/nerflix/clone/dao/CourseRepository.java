package com.nerflix.clone.dao;

import com.nerflix.clone.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c WHERE " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(COALESCE(c.category, '')) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Course> searchAllCourses(@Param("search") String search, Pageable pageable);

    long countByPublishedTrue();

    @Query("SELECT c FROM Course c WHERE c.published = true ORDER BY c.createdAt DESC")
    Page<Course> findPublishedCourses(Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.published = true AND (" +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(COALESCE(c.category, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY c.createdAt DESC")
    Page<Course> searchPublishedCourses(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.published = true AND LOWER(COALESCE(c.category, '')) = LOWER(:category) ORDER BY c.createdAt DESC")
    Page<Course> findPublishedCoursesByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.published = true AND LOWER(COALESCE(c.category, '')) = LOWER(:category) AND (" +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY c.createdAt DESC")
    Page<Course> searchPublishedCoursesByCategory(@Param("category") String category, @Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.published = true ORDER BY FUNCTION('RANDOM')")
    List<Course> findFeaturedCourses(Pageable pageable);
}
