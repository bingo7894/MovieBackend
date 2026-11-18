package com.nerflix.clone.serviceimpl;

import com.nerflix.clone.dao.EpisodeDiscussionRepository;
import com.nerflix.clone.dto.request.DiscussionReplyRequest;
import com.nerflix.clone.dto.request.DiscussionRequest;
import com.nerflix.clone.dto.response.DiscussionResponse;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.enums.Role;
import com.nerflix.clone.exception.ResourceNotFoundException;
import com.nerflix.clone.service.DiscussionService;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiscussionServiceImpl implements DiscussionService {

    @Autowired
    private EpisodeDiscussionRepository discussionRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private LmsMapper lmsMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DiscussionResponse> getEpisodeDiscussions(Long episodeId, String email) {
        Episode episode = serviceUtils.getEpisodeByIdOrThrow(episodeId);
        User user = serviceUtils.getUserByEmailOrThrow(email);
        ensureEpisodeAccessible(episode, user);

        return discussionRepository.findByEpisodeIdOrderByPinnedDescCreatedAtDesc(episodeId).stream()
                .map(d -> lmsMapper.toDiscussionResponse(d, user.getId()))
                .toList();
    }

    @Override
    @Transactional
    public DiscussionResponse createDiscussion(Long episodeId, DiscussionRequest request, String email) {
        Episode episode = serviceUtils.getEpisodeByIdOrThrow(episodeId);
        User user = serviceUtils.getUserByEmailOrThrow(email);
        ensureEpisodeAccessible(episode, user);

        EpisodeDiscussion discussion = new EpisodeDiscussion();
        discussion.setEpisode(episode);
        discussion.setUser(user);
        discussion.setTitle(request.getTitle().trim());
        discussion.setContent(request.getContent().trim());
        discussion.setVideoTimestamp(request.getVideoTimestamp() != null && request.getVideoTimestamp() >= 0
                ? request.getVideoTimestamp()
                : 0);

        EpisodeDiscussion saved = discussionRepository.save(discussion);
        return lmsMapper.toDiscussionResponse(saved, user.getId());
    }

    @Override
    @Transactional
    public DiscussionResponse addReply(Long discussionId, DiscussionReplyRequest request, String email) {
        EpisodeDiscussion discussion = serviceUtils.getDiscussionByIdOrThrow(discussionId);
        User user = serviceUtils.getUserByEmailOrThrow(email);
        ensureEpisodeAccessible(discussion.getEpisode(), user);

        DiscussionReply reply = new DiscussionReply();
        reply.setDiscussion(discussion);
        reply.setUser(user);
        reply.setContent(request.getContent().trim());
        reply.setInstructorReply(user.getRole() == Role.ADMIN);
        discussion.getReplies().add(reply);

        EpisodeDiscussion saved = discussionRepository.save(discussion);
        return lmsMapper.toDiscussionResponse(saved, user.getId());
    }

    @Override
    @Transactional
    public DiscussionResponse toggleResolved(Long discussionId, String email) {
        EpisodeDiscussion discussion = serviceUtils.getDiscussionByIdOrThrow(discussionId);
        User user = serviceUtils.getUserByEmailOrThrow(email);
        ensureOwnerOrAdmin(discussion, user);

        discussion.setResolved(!discussion.isResolved());
        EpisodeDiscussion saved = discussionRepository.save(discussion);
        return lmsMapper.toDiscussionResponse(saved, user.getId());
    }

    @Override
    @Transactional
    public DiscussionResponse togglePinned(Long discussionId, String email) {
        EpisodeDiscussion discussion = serviceUtils.getDiscussionByIdOrThrow(discussionId);
        User user = serviceUtils.getUserByEmailOrThrow(email);
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can pin discussions");
        }
        discussion.setPinned(!discussion.isPinned());
        EpisodeDiscussion saved = discussionRepository.save(discussion);
        return lmsMapper.toDiscussionResponse(saved, user.getId());
    }

    @Override
    @Transactional
    public MessageResponse deleteDiscussion(Long discussionId, String email) {
        EpisodeDiscussion discussion = serviceUtils.getDiscussionByIdOrThrow(discussionId);
        User user = serviceUtils.getUserByEmailOrThrow(email);
        ensureOwnerOrAdmin(discussion, user);

        discussionRepository.delete(discussion);
        return new MessageResponse("Discussion deleted successfully.");
    }

    private void ensureEpisodeAccessible(Episode episode, User user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (!episode.isPublished() || !episode.getTopic().getCourse().isPublished()) {
            throw new ResourceNotFoundException("Episode not found with id: " + episode.getId());
        }
    }

    private void ensureOwnerOrAdmin(EpisodeDiscussion discussion, User user) {
        boolean isOwner = discussion.getUser().getId().equals(user.getId());
        if (!isOwner && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You cannot modify this discussion");
        }
    }
}
