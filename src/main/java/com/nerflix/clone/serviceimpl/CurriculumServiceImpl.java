package com.nerflix.clone.serviceimpl;

import com.nerflix.clone.dao.*;
import com.nerflix.clone.dto.request.AttachmentRequest;
import com.nerflix.clone.dto.request.EpisodeRequest;
import com.nerflix.clone.dto.request.ReorderRequest;
import com.nerflix.clone.dto.request.TopicRequest;
import com.nerflix.clone.dto.response.AttachmentResponse;
import com.nerflix.clone.dto.response.EpisodeResponse;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.TopicResponse;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.service.CurriculumService;
import com.nerflix.clone.util.LmsDataHelper;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurriculumServiceImpl implements CurriculumService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private LessonAttachmentRepository attachmentRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private LmsMapper lmsMapper;

    @Autowired
    private LmsDataHelper lmsDataHelper;

    @Override
    @Transactional
    public TopicResponse createTopic(Long courseId, TopicRequest request) {
        Course course = serviceUtils.getCourseByIdOrThrow(courseId);
        Topic topic = new Topic();
        topic.setTitle(request.getTitle().trim());
        topic.setDescription(request.getDescription());
        int nextOrder = request.getSortOrder() != null
                ? request.getSortOrder()
                : course.getTopics().size() + 1;
        topic.setSortOrder(nextOrder);
        topic.setCourse(course);

        Topic saved = topicRepository.save(topic);
        return lmsMapper.toTopicResponse(saved, serviceUtils.buildBaseUrl(), Map.of(), Map.of(), true);
    }

    @Override
    @Transactional
    public TopicResponse updateTopic(Long topicId, TopicRequest request) {
        Topic topic = serviceUtils.getTopicByIdOrThrow(topicId);
        if (request.getTitle() != null) {
            topic.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            topic.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            topic.setSortOrder(request.getSortOrder());
        }
        Topic saved = topicRepository.save(topic);
        return lmsMapper.toTopicResponse(saved, serviceUtils.buildBaseUrl(), Map.of(), Map.of(), true);
    }

    @Override
    @Transactional
    public MessageResponse deleteTopic(Long topicId) {
        Topic topic = serviceUtils.getTopicByIdOrThrow(topicId);
        List<Long> episodeIds = episodeRepository.findEpisodeIdsByTopicId(topicId);
        lmsDataHelper.deleteEpisodeDependencies(episodeIds);
        topicRepository.delete(topic);
        return new MessageResponse("Topic deleted successfully.");
    }

    @Override
    @Transactional
    public MessageResponse reorderTopics(Long courseId, ReorderRequest request) {
        serviceUtils.getCourseByIdOrThrow(courseId);
        List<Topic> topics = topicRepository.findByCourseIdOrderBySortOrderAscIdAsc(courseId);
        Map<Long, Integer> orderMap = buildOrderMap(request.getOrderedIds());
        for (Topic topic : topics) {
            Integer newOrder = orderMap.get(topic.getId());
            if (newOrder != null) {
                topic.setSortOrder(newOrder);
            }
        }
        topicRepository.saveAll(topics);
        return new MessageResponse("Topics reordered successfully.");
    }

    @Override
    @Transactional
    public EpisodeResponse createEpisode(Long topicId, EpisodeRequest request) {
        Topic topic = serviceUtils.getTopicByIdOrThrow(topicId);
        Episode ep = new Episode();
        applyEpisodeFields(ep, request);
        if (request.getEpisodeNumber() == null) {
            ep.setEpisodeNumber(topic.getEpisodes().size() + 1);
        }
        ep.setTopic(topic);
        Episode saved = episodeRepository.save(ep);
        return lmsMapper.toEpisodeResponse(saved, serviceUtils.buildBaseUrl(), null, Map.of(), true);
    }

    @Override
    @Transactional
    public EpisodeResponse updateEpisode(Long episodeId, EpisodeRequest request) {
        Episode ep = serviceUtils.getEpisodeByIdOrThrow(episodeId);
        applyEpisodeFields(ep, request);
        Episode saved = episodeRepository.save(ep);
        return lmsMapper.toEpisodeResponse(saved, serviceUtils.buildBaseUrl(), null, Map.of(), true);
    }

    @Override
    @Transactional
    public MessageResponse deleteEpisode(Long episodeId) {
        Episode ep = serviceUtils.getEpisodeByIdOrThrow(episodeId);
        lmsDataHelper.deleteEpisodeDependencies(List.of(episodeId));
        episodeRepository.delete(ep);
        return new MessageResponse("Episode deleted successfully.");
    }

    @Override
    @Transactional
    public MessageResponse reorderEpisodes(Long topicId, ReorderRequest request) {
        serviceUtils.getTopicByIdOrThrow(topicId);
        List<Episode> episodes = episodeRepository.findByTopicIdOrderByEpisodeNumberAscIdAsc(topicId);
        Map<Long, Integer> orderMap = buildOrderMap(request.getOrderedIds());
        for (Episode ep : episodes) {
            Integer newNumber = orderMap.get(ep.getId());
            if (newNumber != null) {
                ep.setEpisodeNumber(newNumber);
            }
        }
        episodeRepository.saveAll(episodes);
        return new MessageResponse("Episodes reordered successfully.");
    }

    @Override
    @Transactional
    public AttachmentResponse addAttachment(Long episodeId, AttachmentRequest request) {
        Episode ep = serviceUtils.getEpisodeByIdOrThrow(episodeId);
        LessonAttachment attachment = new LessonAttachment();
        attachment.setTitle(request.getTitle().trim());
        attachment.setOriginalFilename(request.getOriginalFilename().trim());
        attachment.setFileUuid(serviceUtils.extractUuid(request.getFileUuid()));
        attachment.setFileSize(request.getFileSize());
        attachment.setContentType(request.getContentType());
        attachment.setEpisode(ep);

        LessonAttachment saved = attachmentRepository.save(attachment);
        return lmsMapper.toAttachmentResponse(saved, serviceUtils.buildBaseUrl());
    }

    @Override
    @Transactional
    public MessageResponse deleteAttachment(Long attachmentId) {
        LessonAttachment attachment = serviceUtils.getAttachmentByIdOrThrow(attachmentId);
        attachmentRepository.delete(attachment);
        return new MessageResponse("Attachment deleted successfully.");
    }

    private void applyEpisodeFields(Episode ep, EpisodeRequest request) {
        if (request.getTitle() != null) {
            ep.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            ep.setDescription(request.getDescription());
        }
        if (request.getEpisodeNumber() != null) {
            ep.setEpisodeNumber(request.getEpisodeNumber());
        }
        if (request.getVideoUuid() != null) {
            ep.setVideoUuid(serviceUtils.extractUuid(request.getVideoUuid()));
        }
        if (request.getDuration() != null) {
            ep.setDuration(request.getDuration());
        }
        if (request.getFreePreview() != null) {
            ep.setFreePreview(request.getFreePreview());
        }
        if (request.getPublished() != null) {
            ep.setPublished(request.getPublished());
        }
    }

    private Map<Long, Integer> buildOrderMap(List<Long> orderedIds) {
        Map<Long, Integer> map = new HashMap<>();
        for (int i = 0; i < orderedIds.size(); i++) {
            map.put(orderedIds.get(i), i + 1);
        }
        return map;
    }
}
