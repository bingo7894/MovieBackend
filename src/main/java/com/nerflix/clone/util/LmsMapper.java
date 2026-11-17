package com.nerflix.clone.util;

import com.nerflix.clone.dto.response.*;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.enums.CourseLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class LmsMapper {

    @Autowired
    private ServiceUtils serviceUtils;

    public CourseSummaryResponse toCourseSummary(
            Course course,
            boolean enrolled,
            Map<Long, LessonProgress> progressByEpisodeId,
            boolean includeUnpublishedEpisodes) {

        int topicCount = course.getTopics().size();
        int episodeCount = 0;
        long totalDuration = 0;
        int completedEpisodes = 0;

        for (Topic topic : course.getTopics()) {
            for (Episode ep : topic.getEpisodes()) {
                if (!includeUnpublishedEpisodes && !ep.isPublished()) {
                    continue;
                }
                episodeCount++;
                totalDuration += ep.getDuration() != null ? ep.getDuration() : 0;
                LessonProgress progress = progressByEpisodeId.get(ep.getId());
                if (progress != null && progress.isCompleted()) {
                    completedEpisodes++;
                }
            }
        }

        int progressPercent = episodeCount > 0 ? Math.round((completedEpisodes * 100.0f) / episodeCount) : 0;
        String baseUrl = serviceUtils.buildBaseUrl();
        String thumbnailUrl = buildImageUrl(baseUrl, course.getThumbnailUuid());

        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                thumbnailUrl,
                course.getThumbnailUuid(),
                course.getCategory(),
                course.getLevel() != null ? course.getLevel().name() : CourseLevel.BEGINNER.name(),
                course.isPublished(),
                topicCount,
                episodeCount,
                totalDuration,
                enrolled,
                completedEpisodes,
                progressPercent,
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }

    public CourseDetailResponse toCourseDetail(
            Course course,
            boolean enrolled,
            Map<Long, LessonProgress> progressByEpisodeId,
            Map<Long, List<QuizAttempt>> attemptsByQuizId,
            boolean includeUnpublished) {

        String baseUrl = serviceUtils.buildBaseUrl();
        List<TopicResponse> topicResponses = new ArrayList<>();
        int totalEpisodes = 0;
        int totalCompleted = 0;
        long courseDuration = 0;

        for (Topic topic : course.getTopics()) {
            TopicResponse topicResp = toTopicResponse(
                    topic, baseUrl, progressByEpisodeId, attemptsByQuizId, includeUnpublished);
            if (!includeUnpublished && topicResp.getEpisodes().isEmpty()) {
                // Still show empty topics in admin; for learners keep topic so structure is visible
            }
            totalEpisodes += topicResp.getEpisodes().size();
            totalCompleted += topicResp.getCompletedEpisodes();
            courseDuration += topicResp.getTotalDuration();
            topicResponses.add(topicResp);
        }

        int progressPercent = totalEpisodes > 0 ? Math.round((totalCompleted * 100.0f) / totalEpisodes) : 0;

        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                buildImageUrl(baseUrl, course.getThumbnailUuid()),
                course.getThumbnailUuid(),
                course.getCategory(),
                course.getLevel() != null ? course.getLevel().name() : CourseLevel.BEGINNER.name(),
                course.isPublished(),
                topicResponses.size(),
                totalEpisodes,
                courseDuration,
                enrolled,
                totalCompleted,
                progressPercent,
                course.getCreatedAt(),
                course.getUpdatedAt(),
                topicResponses
        );
    }

    public TopicResponse toTopicResponse(
            Topic topic,
            String baseUrl,
            Map<Long, LessonProgress> progressByEpisodeId,
            Map<Long, List<QuizAttempt>> attemptsByQuizId,
            boolean includeUnpublished) {

        List<EpisodeResponse> episodeResponses = new ArrayList<>();
        long topicDuration = 0;
        int completedCount = 0;

        for (Episode ep : topic.getEpisodes()) {
            if (!includeUnpublished && !ep.isPublished()) {
                continue;
            }
            EpisodeResponse epResp = toEpisodeResponse(
                    ep, baseUrl, progressByEpisodeId.get(ep.getId()), attemptsByQuizId, includeUnpublished);
            topicDuration += epResp.getDuration() != null ? epResp.getDuration() : 0;
            if (epResp.isCompleted()) {
                completedCount++;
            }
            episodeResponses.add(epResp);
        }

        return new TopicResponse(
                topic.getId(),
                topic.getTitle(),
                topic.getDescription(),
                topic.getSortOrder(),
                topic.getCourse().getId(),
                topicDuration,
                completedCount,
                episodeResponses
        );
    }

    public EpisodeResponse toEpisodeResponse(
            Episode ep,
            String baseUrl,
            LessonProgress progress,
            Map<Long, List<QuizAttempt>> attemptsByQuizId,
            boolean includeUnpublished) {

        List<AttachmentResponse> attachmentResponses = ep.getAttachments().stream()
                .map(att -> toAttachmentResponse(att, baseUrl))
                .toList();

        List<QuizSummaryResponse> quizSummaries = ep.getQuizzes().stream()
                .filter(q -> includeUnpublished || q.isPublished())
                .map(q -> toQuizSummary(q, attemptsByQuizId.getOrDefault(q.getId(), List.of())))
                .toList();

        return new EpisodeResponse(
                ep.getId(),
                ep.getTitle(),
                ep.getDescription(),
                ep.getEpisodeNumber(),
                buildVideoUrl(baseUrl, ep.getVideoUuid()),
                ep.getVideoUuid(),
                ep.getDuration() != null ? ep.getDuration() : 0,
                ep.isFreePreview(),
                ep.isPublished(),
                ep.getTopic().getId(),
                progress != null ? progress.getWatchedSeconds() : 0,
                progress != null && progress.isCompleted(),
                attachmentResponses,
                quizSummaries
        );
    }

    public AttachmentResponse toAttachmentResponse(LessonAttachment att, String baseUrl) {
        String encodedFilename = URLEncoder.encode(att.getOriginalFilename(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String fileUrl = baseUrl + "/api/files/material/" + att.getFileUuid() + "?filename=" + encodedFilename;
        String downloadUrl = fileUrl + "&download=true";

        return new AttachmentResponse(
                att.getId(),
                att.getTitle(),
                att.getOriginalFilename(),
                att.getFileUuid(),
                fileUrl,
                downloadUrl,
                att.getFileSize(),
                att.getContentType(),
                att.getEpisode().getId(),
                att.getCreatedAt()
        );
    }

    public QuizSummaryResponse toQuizSummary(Quiz quiz, List<QuizAttempt> attempts) {
        Integer bestScore = null;
        Boolean passed = null;
        if (!attempts.isEmpty()) {
            bestScore = attempts.stream()
                    .mapToInt(QuizAttempt::getScorePercent)
                    .max()
                    .orElse(0);
            passed = attempts.stream().anyMatch(QuizAttempt::isPassed);
        }
        return new QuizSummaryResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getPassingScore(),
                quiz.isPublished(),
                quiz.getQuestions().size(),
                bestScore,
                passed
        );
    }

    public QuizDetailResponse toQuizDetail(Quiz quiz, boolean includeAnswers) {
        List<QuizQuestionResponse> questionResponses = quiz.getQuestions().stream()
                .map(q -> new QuizQuestionResponse(
                        q.getId(),
                        q.getQuestionText(),
                        q.getQuestionType().name(),
                        includeAnswers ? q.getExplanation() : null,
                        q.getSortOrder(),
                        q.getOptions().stream()
                                .map(opt -> new QuizOptionResponse(
                                        opt.getId(),
                                        opt.getOptionText(),
                                        includeAnswers ? opt.isCorrect() : null))
                                .toList()
                ))
                .toList();

        return new QuizDetailResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getPassingScore(),
                quiz.isPublished(),
                quiz.getEpisode().getId(),
                questionResponses
        );
    }

    public LessonNoteResponse toNoteResponse(LessonNote note) {
        return new LessonNoteResponse(
                note.getId(),
                note.getEpisode().getId(),
                note.getTimestampSeconds(),
                note.getContent(),
                note.getCreatedAt()
        );
    }

    private String buildImageUrl(String baseUrl, String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return null;
        }
        return baseUrl + "/api/files/image/" + uuid;
    }

    private String buildVideoUrl(String baseUrl, String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return null;
        }
        return baseUrl + "/api/files/video/" + uuid;
    }
}
