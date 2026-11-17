package com.nerflix.clone.controller;

import com.nerflix.clone.dto.request.LessonNoteRequest;
import com.nerflix.clone.dto.request.ProgressUpdateRequest;
import com.nerflix.clone.dto.response.*;
import com.nerflix.clone.service.LearningProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Learning Progress & Notes", description = "Track episode progress, enroll in courses, and manage timestamped notes")
public class LearningProgressController {

    @Autowired
    private LearningProgressService learningProgressService;

    @Operation(summary = "Update watched seconds or completion status for an episode")
    @PostMapping("/progress/episodes/{episodeId}")
    public ResponseEntity<LessonProgressResponse> updateProgress(
            @PathVariable Long episodeId,
            @RequestBody ProgressUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                learningProgressService.updateProgress(episodeId, request, authentication.getName()));
    }

    @Operation(summary = "Enroll / bookmark a course in My Learning")
    @PostMapping("/enrollments/{courseId}")
    public ResponseEntity<MessageResponse> enrollCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        return ResponseEntity.ok(learningProgressService.enrollCourse(courseId, authentication.getName()));
    }

    @Operation(summary = "Remove a course from My Learning")
    @DeleteMapping("/enrollments/{courseId}")
    public ResponseEntity<MessageResponse> unenrollCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        return ResponseEntity.ok(learningProgressService.unenrollCourse(courseId, authentication.getName()));
    }

    @Operation(summary = "List enrolled courses with progress")
    @GetMapping("/enrollments")
    public ResponseEntity<PageResponse<CourseSummaryResponse>> getEnrolledCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        return ResponseEntity.ok(
                learningProgressService.getEnrolledCourses(authentication.getName(), page, size, search));
    }

    @Operation(summary = "Get personal notes for an episode")
    @GetMapping("/notes/episodes/{episodeId}")
    public ResponseEntity<List<LessonNoteResponse>> getNotes(
            @PathVariable Long episodeId,
            Authentication authentication) {
        return ResponseEntity.ok(learningProgressService.getNotes(episodeId, authentication.getName()));
    }

    @Operation(summary = "Add a timestamped note to an episode")
    @PostMapping("/notes/episodes/{episodeId}")
    public ResponseEntity<LessonNoteResponse> createNote(
            @PathVariable Long episodeId,
            @Valid @RequestBody LessonNoteRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(learningProgressService.createNote(episodeId, request, authentication.getName()));
    }

    @Operation(summary = "Delete a personal note")
    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<MessageResponse> deleteNote(
            @PathVariable Long noteId,
            Authentication authentication) {
        return ResponseEntity.ok(learningProgressService.deleteNote(noteId, authentication.getName()));
    }
}
