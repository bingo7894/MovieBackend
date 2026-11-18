package com.nerflix.clone.controller;

import com.nerflix.clone.dto.response.AdminAnalyticsOverviewResponse;
import com.nerflix.clone.dto.response.CourseLearnerProgressResponse;
import com.nerflix.clone.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Analytics", description = "Learner progress reports, quiz pass rates, and CSV exports")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Operation(summary = "[Admin] Get platform-wide LMS analytics overview")
    @GetMapping("/overview")
    public ResponseEntity<AdminAnalyticsOverviewResponse> getOverview() {
        return ResponseEntity.ok(analyticsService.getOverview());
    }

    @Operation(summary = "[Admin] Get learner progress breakdown for a course")
    @GetMapping("/courses/{courseId}/learners")
    public ResponseEntity<List<CourseLearnerProgressResponse>> getCourseLearners(@PathVariable Long courseId) {
        return ResponseEntity.ok(analyticsService.getCourseLearners(courseId));
    }

    @Operation(summary = "[Admin] Export course learner progress as CSV")
    @GetMapping("/courses/{courseId}/export")
    public ResponseEntity<byte[]> exportCourseLearners(@PathVariable Long courseId) {
        byte[] csv = analyticsService.exportCourseLearnersCsv(courseId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"course-" + courseId + "-learners.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
