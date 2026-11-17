package com.nerflix.clone.controller;

import com.nerflix.clone.dto.request.AttachmentRequest;
import com.nerflix.clone.dto.request.EpisodeRequest;
import com.nerflix.clone.dto.request.ReorderRequest;
import com.nerflix.clone.dto.request.TopicRequest;
import com.nerflix.clone.dto.response.AttachmentResponse;
import com.nerflix.clone.dto.response.EpisodeResponse;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.TopicResponse;
import com.nerflix.clone.service.CurriculumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Curriculum (Admin)", description = "Manage topics, episodes (EP), and learning material attachments")
public class CurriculumController {

    @Autowired
    private CurriculumService curriculumService;

    @Operation(summary = "Create a topic inside a course")
    @PostMapping("/courses/{courseId}/topics")
    public ResponseEntity<TopicResponse> createTopic(
            @PathVariable Long courseId,
            @Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(curriculumService.createTopic(courseId, request));
    }

    @Operation(summary = "Update a topic")
    @PutMapping("/topics/{topicId}")
    public ResponseEntity<TopicResponse> updateTopic(
            @PathVariable Long topicId,
            @Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(curriculumService.updateTopic(topicId, request));
    }

    @Operation(summary = "Delete a topic and its episodes")
    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<MessageResponse> deleteTopic(@PathVariable Long topicId) {
        return ResponseEntity.ok(curriculumService.deleteTopic(topicId));
    }

    @Operation(summary = "Reorder topics inside a course")
    @PatchMapping("/courses/{courseId}/topics/reorder")
    public ResponseEntity<MessageResponse> reorderTopics(
            @PathVariable Long courseId,
            @Valid @RequestBody ReorderRequest request) {
        return ResponseEntity.ok(curriculumService.reorderTopics(courseId, request));
    }

    @Operation(summary = "Create an episode (EP) inside a topic")
    @PostMapping("/topics/{topicId}/episodes")
    public ResponseEntity<EpisodeResponse> createEpisode(
            @PathVariable Long topicId,
            @Valid @RequestBody EpisodeRequest request) {
        return ResponseEntity.ok(curriculumService.createEpisode(topicId, request));
    }

    @Operation(summary = "Update an episode (EP)")
    @PutMapping("/episodes/{episodeId}")
    public ResponseEntity<EpisodeResponse> updateEpisode(
            @PathVariable Long episodeId,
            @Valid @RequestBody EpisodeRequest request) {
        return ResponseEntity.ok(curriculumService.updateEpisode(episodeId, request));
    }

    @Operation(summary = "Delete an episode (EP)")
    @DeleteMapping("/episodes/{episodeId}")
    public ResponseEntity<MessageResponse> deleteEpisode(@PathVariable Long episodeId) {
        return ResponseEntity.ok(curriculumService.deleteEpisode(episodeId));
    }

    @Operation(summary = "Reorder episodes inside a topic")
    @PatchMapping("/topics/{topicId}/episodes/reorder")
    public ResponseEntity<MessageResponse> reorderEpisodes(
            @PathVariable Long topicId,
            @Valid @RequestBody ReorderRequest request) {
        return ResponseEntity.ok(curriculumService.reorderEpisodes(topicId, request));
    }

    @Operation(summary = "Attach a learning material file to an episode")
    @PostMapping("/episodes/{episodeId}/attachments")
    public ResponseEntity<AttachmentResponse> addAttachment(
            @PathVariable Long episodeId,
            @Valid @RequestBody AttachmentRequest request) {
        return ResponseEntity.ok(curriculumService.addAttachment(episodeId, request));
    }

    @Operation(summary = "Remove a learning material attachment")
    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<MessageResponse> deleteAttachment(@PathVariable Long attachmentId) {
        return ResponseEntity.ok(curriculumService.deleteAttachment(attachmentId));
    }
}
