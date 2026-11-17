package com.nerflix.clone.controller;

import com.nerflix.clone.dto.request.CourseRequest;
import com.nerflix.clone.dto.response.*;
import com.nerflix.clone.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Courses", description = "Course catalog and admin course management")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Operation(summary = "Browse published courses with progress")
    @GetMapping("/courses")
    public ResponseEntity<PageResponse<CourseSummaryResponse>> getPublishedCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Authentication authentication) {
        return ResponseEntity.ok(
                courseService.getPublishedCourses(page, size, search, category, authentication.getName()));
    }

    @Operation(summary = "Get featured published courses")
    @GetMapping("/courses/featured")
    public ResponseEntity<List<CourseSummaryResponse>> getFeaturedCourses(Authentication authentication) {
        return ResponseEntity.ok(courseService.getFeaturedCourses(authentication.getName()));
    }

    @Operation(summary = "Get full course curriculum (topics, episodes, attachments, quizzes, progress)")
    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseDetailResponse> getCourseDetailForLearner(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(courseService.getCourseDetailForLearner(id, authentication.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] List all courses including drafts")
    @GetMapping("/admin/courses")
    public ResponseEntity<PageResponse<CourseSummaryResponse>> getAllAdminCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(courseService.getAllAdminCourses(page, size, search));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Get full course curriculum for editing")
    @GetMapping("/admin/courses/{id}")
    public ResponseEntity<CourseDetailResponse> getCourseDetailForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseDetailForAdmin(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Get LMS course statistics")
    @GetMapping("/admin/courses/stats")
    public ResponseEntity<CourseStatsResponse> getAdminStats() {
        return ResponseEntity.ok(courseService.getAdminStats());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Create a new course")
    @PostMapping("/admin/courses")
    public ResponseEntity<CourseSummaryResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.createCourse(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Update course details")
    @PutMapping("/admin/courses/{id}")
    public ResponseEntity<CourseSummaryResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Toggle course publish status")
    @PatchMapping("/admin/courses/{id}/publish")
    public ResponseEntity<MessageResponse> toggleCoursePublish(
            @PathVariable Long id,
            @RequestParam boolean value) {
        return ResponseEntity.ok(courseService.toggleCoursePublish(id, value));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Delete a course and all its content")
    @DeleteMapping("/admin/courses/{id}")
    public ResponseEntity<MessageResponse> deleteCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.deleteCourse(id));
    }
}
