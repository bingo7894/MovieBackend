package com.nerflix.clone.util;

import com.nerflix.clone.dao.*;
import com.nerflix.clone.dto.response.CourseSummaryResponse;
import com.nerflix.clone.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LmsDataHelper {

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private LessonNoteRepository lessonNoteRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EpisodeDiscussionRepository episodeDiscussionRepository;

    @Autowired
    private DiscussionReplyRepository discussionReplyRepository;

    @Autowired
    private CourseReviewRepository courseReviewRepository;

    public Map<Long, LessonProgress> loadProgressMapForCourses(Long userId, List<Course> courses) {
        if (userId == null || courses.isEmpty()) {
            return Map.of();
        }
        List<Long> episodeIds = courses.stream()
                .flatMap(c -> c.getTopics().stream())
                .flatMap(t -> t.getEpisodes().stream())
                .map(Episode::getId)
                .toList();
        return loadProgressMapForEpisodeIds(userId, episodeIds);
    }

    public Map<Long, LessonProgress> loadProgressMapForEpisodeIds(Long userId, List<Long> episodeIds) {
        if (userId == null || episodeIds.isEmpty()) {
            return Map.of();
        }
        return lessonProgressRepository.findByUserIdAndEpisodeIdIn(userId, episodeIds).stream()
                .collect(Collectors.toMap(lp -> lp.getEpisode().getId(), Function.identity(), (a, b) -> a));
    }

    public Map<Long, List<QuizAttempt>> loadAttemptsMapForCourse(Long userId, Course course) {
        if (userId == null) {
            return Map.of();
        }
        List<Long> quizIds = course.getTopics().stream()
                .flatMap(t -> t.getEpisodes().stream())
                .flatMap(e -> e.getQuizzes().stream())
                .map(Quiz::getId)
                .toList();
        if (quizIds.isEmpty()) {
            return Map.of();
        }
        return quizAttemptRepository.findByUserIdAndQuizIdIn(userId, quizIds).stream()
                .collect(Collectors.groupingBy(a -> a.getQuiz().getId()));
    }

    public Set<Long> loadEnrolledCourseIds(Long userId, List<Course> courses) {
        if (userId == null || courses.isEmpty()) {
            return Set.of();
        }
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        return enrollmentRepository.findEnrolledCourseIds(userId, courseIds);
    }

    public void enrichCourseSummaryStats(List<CourseSummaryResponse> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return;
        }
        List<Long> courseIds = summaries.stream().map(CourseSummaryResponse::getId).toList();

        Map<Long, double[]> reviewStats = new HashMap<>();
        for (Object[] row : courseReviewRepository.getReviewStatsByCourseIds(courseIds)) {
            Long courseId = ((Number) row[0]).longValue();
            double avg = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            double roundedAvg = Math.round(avg * 10.0) / 10.0;
            double count = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            reviewStats.put(courseId, new double[]{roundedAvg, count});
        }

        Map<Long, Long> enrollmentCounts = new HashMap<>();
        for (Object[] row : enrollmentRepository.getEnrollmentCountsByCourseIds(courseIds)) {
            Long courseId = ((Number) row[0]).longValue();
            long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            enrollmentCounts.put(courseId, count);
        }

        for (CourseSummaryResponse s : summaries) {
            double[] stats = reviewStats.get(s.getId());
            if (stats != null) {
                s.setAverageRating(stats[0]);
                s.setReviewCount((long) stats[1]);
            }
            s.setEnrollmentCount(enrollmentCounts.getOrDefault(s.getId(), 0L));
        }
    }

    public void deleteEpisodeDependencies(List<Long> episodeIds) {
        if (episodeIds == null || episodeIds.isEmpty()) {
            return;
        }
        List<Long> discussionIds = episodeDiscussionRepository.findIdsByEpisodeIdIn(episodeIds);
        if (!discussionIds.isEmpty()) {
            discussionReplyRepository.deleteByDiscussionIdIn(discussionIds);
        }
        episodeDiscussionRepository.deleteByEpisodeIdIn(episodeIds);

        List<Long> quizIds = quizRepository.findQuizIdsByEpisodeIds(episodeIds);
        if (!quizIds.isEmpty()) {
            quizAttemptRepository.deleteByQuizIdIn(quizIds);
        }
        lessonNoteRepository.deleteByEpisodeIdIn(episodeIds);
        lessonProgressRepository.deleteByEpisodeIdIn(episodeIds);
    }
}
