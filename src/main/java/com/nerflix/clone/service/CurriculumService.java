package com.nerflix.clone.service;

import com.nerflix.clone.dto.request.AttachmentRequest;
import com.nerflix.clone.dto.request.EpisodeRequest;
import com.nerflix.clone.dto.request.ReorderRequest;
import com.nerflix.clone.dto.request.TopicRequest;
import com.nerflix.clone.dto.response.AttachmentResponse;
import com.nerflix.clone.dto.response.EpisodeResponse;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.TopicResponse;

public interface CurriculumService {
    TopicResponse createTopic(Long courseId, TopicRequest request);

    TopicResponse updateTopic(Long topicId, TopicRequest request);

    MessageResponse deleteTopic(Long topicId);

    MessageResponse reorderTopics(Long courseId, ReorderRequest request);

    EpisodeResponse createEpisode(Long topicId, EpisodeRequest request);

    EpisodeResponse updateEpisode(Long episodeId, EpisodeRequest request);

    MessageResponse deleteEpisode(Long episodeId);

    MessageResponse reorderEpisodes(Long topicId, ReorderRequest request);

    AttachmentResponse addAttachment(Long episodeId, AttachmentRequest request);

    MessageResponse deleteAttachment(Long attachmentId);
}
