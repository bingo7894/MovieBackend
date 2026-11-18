package com.nerflix.clone.service;

import com.nerflix.clone.dto.request.DiscussionReplyRequest;
import com.nerflix.clone.dto.request.DiscussionRequest;
import com.nerflix.clone.dto.response.DiscussionResponse;
import com.nerflix.clone.dto.response.MessageResponse;

import java.util.List;

public interface DiscussionService {
    List<DiscussionResponse> getEpisodeDiscussions(Long episodeId, String email);

    DiscussionResponse createDiscussion(Long episodeId, DiscussionRequest request, String email);

    DiscussionResponse addReply(Long discussionId, DiscussionReplyRequest request, String email);

    DiscussionResponse toggleResolved(Long discussionId, String email);

    DiscussionResponse togglePinned(Long discussionId, String email);

    MessageResponse deleteDiscussion(Long discussionId, String email);
}
