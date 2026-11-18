package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionReplyResponse {
    private Long id;
    private Long discussionId;
    private Long authorId;
    private String authorName;
    private boolean instructorReply;
    private String content;
    private boolean ownReply;
    private Instant createdAt;
}
