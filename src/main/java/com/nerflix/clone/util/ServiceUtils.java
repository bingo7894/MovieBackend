package com.nerflix.clone.util;

import com.nerflix.clone.dao.*;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class ServiceUtils {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private LessonAttachmentRepository attachmentRepository;

    @Autowired
    private QuizRepository quizRepository;

    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public Course getCourseByIdOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    public Topic getTopicByIdOrThrow(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));
    }

    public Episode getEpisodeByIdOrThrow(Long id) {
        return episodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found with id: " + id));
    }

    public LessonAttachment getAttachmentByIdOrThrow(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + id));
    }

    public Quiz getQuizByIdOrThrow(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
    }

    public String extractUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        int queryIdx = trimmed.indexOf('?');
        if (queryIdx >= 0) {
            trimmed = trimmed.substring(0, queryIdx);
        }
        int slashIdx = trimmed.lastIndexOf('/');
        if (slashIdx >= 0 && slashIdx < trimmed.length() - 1) {
            return trimmed.substring(slashIdx + 1);
        }
        return trimmed;
    }

    public String buildBaseUrl() {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        } catch (Exception ex) {
            return "";
        }
    }
}
