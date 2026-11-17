package com.nerflix.clone.controller;

import com.nerflix.clone.dto.request.QuizRequest;
import com.nerflix.clone.dto.request.QuizSubmitRequest;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.QuizAttemptSummaryResponse;
import com.nerflix.clone.dto.response.QuizDetailResponse;
import com.nerflix.clone.dto.response.QuizResultResponse;
import com.nerflix.clone.service.QuizService;
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
@Tag(name = "Quizzes & Exercises", description = "Create quizzes, take exercises, auto-grade, and view attempts")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Create a quiz for an episode")
    @PostMapping("/admin/episodes/{episodeId}/quizzes")
    public ResponseEntity<QuizDetailResponse> createQuiz(
            @PathVariable Long episodeId,
            @Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.createQuiz(episodeId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Get quiz with answers for editing")
    @GetMapping("/admin/quizzes/{quizId}")
    public ResponseEntity<QuizDetailResponse> getQuizForAdmin(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizForAdmin(quizId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Update a quiz and its questions/options")
    @PutMapping("/admin/quizzes/{quizId}")
    public ResponseEntity<QuizDetailResponse> updateQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.updateQuiz(quizId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Delete a quiz")
    @DeleteMapping("/admin/quizzes/{quizId}")
    public ResponseEntity<MessageResponse> deleteQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.deleteQuiz(quizId));
    }

    @Operation(summary = "Get quiz questions for learner (without answer keys)")
    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizDetailResponse> getQuizForLearner(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizForLearner(quizId));
    }

    @Operation(summary = "Submit quiz answers and get graded result with explanations")
    @PostMapping("/quizzes/{quizId}/submit")
    public ResponseEntity<QuizResultResponse> submitQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmitRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(quizService.submitQuiz(quizId, request, authentication.getName()));
    }

    @Operation(summary = "Get current learner's past attempts for a quiz")
    @GetMapping("/quizzes/{quizId}/attempts")
    public ResponseEntity<List<QuizAttemptSummaryResponse>> getUserAttempts(
            @PathVariable Long quizId,
            Authentication authentication) {
        return ResponseEntity.ok(quizService.getUserAttempts(quizId, authentication.getName()));
    }
}
