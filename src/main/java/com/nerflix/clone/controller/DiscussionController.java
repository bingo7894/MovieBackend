package com.nerflix.clone.controller;

import com.nerflix.clone.dto.request.DiscussionReplyRequest;
import com.nerflix.clone.dto.request.DiscussionRequest;
import com.nerflix.clone.dto.response.DiscussionResponse;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.service.DiscussionService;
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
@Tag(name = "Episode Q&A Discussions", description = "Learner and instructor Q&A threads per episode")
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    @Operation(summary = "List Q&A discussions for an episode")
    @GetMapping("/episodes/{episodeId}/discussions")
    public ResponseEntity<List<DiscussionResponse>> getEpisodeDiscussions(
            @PathVariable Long episodeId,
            Authentication authentication) {
        return ResponseEntity.ok(discussionService.getEpisodeDiscussions(episodeId, authentication.getName()));
    }

    @Operation(summary = "Ask a question in an episode discussion")
    @PostMapping("/episodes/{episodeId}/discussions")
    public ResponseEntity<DiscussionResponse> createDiscussion(
            @PathVariable Long episodeId,
            @Valid @RequestBody DiscussionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(discussionService.createDiscussion(episodeId, request, authentication.getName()));
    }

    @Operation(summary = "Reply to an episode discussion")
    @PostMapping("/discussions/{discussionId}/replies")
    public ResponseEntity<DiscussionResponse> addReply(
            @PathVariable Long discussionId,
            @Valid @RequestBody DiscussionReplyRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(discussionService.addReply(discussionId, request, authentication.getName()));
    }

    @Operation(summary = "Mark a discussion as resolved or unresolved")
    @PatchMapping("/discussions/{discussionId}/resolve")
    public ResponseEntity<DiscussionResponse> toggleResolved(
            @PathVariable Long discussionId,
            Authentication authentication) {
        return ResponseEntity.ok(discussionService.toggleResolved(discussionId, authentication.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Pin or unpin a discussion thread")
    @PatchMapping("/discussions/{discussionId}/pin")
    public ResponseEntity<DiscussionResponse> togglePinned(
            @PathVariable Long discussionId,
            Authentication authentication) {
        return ResponseEntity.ok(discussionService.togglePinned(discussionId, authentication.getName()));
    }

    @Operation(summary = "Delete a discussion thread (owner or admin)")
    @DeleteMapping("/discussions/{discussionId}")
    public ResponseEntity<MessageResponse> deleteDiscussion(
            @PathVariable Long discussionId,
            Authentication authentication) {
        return ResponseEntity.ok(discussionService.deleteDiscussion(discussionId, authentication.getName()));
    }
}
