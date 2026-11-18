package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionResponse {
    private Long id;
    private Long episodeId;
    private Long authorId;
    private String authorName;
    private boolean authorIsAdmin;
    private String title;
    private String content;
    private Integer videoTimestamp;
    private boolean resolved;
    private boolean pinned;
    private boolean ownDiscussion;
    private Instant createdAt;
    private Instant updatedAt;
    private List<DiscussionReplyResponse> replies = new ArrayList<>();
}
