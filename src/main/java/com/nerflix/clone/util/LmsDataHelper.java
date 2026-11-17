package com.nerflix.clone.util;

import com.nerflix.clone.dao.*;
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

    public void deleteEpisodeDependencies(List<Long> episodeIds) {
        if (episodeIds == null || episodeIds.isEmpty()) {
            return;
        }
        List<Long> quizIds = quizRepository.findQuizIdsByEpisodeIds(episodeIds);
        if (!quizIds.isEmpty()) {
            quizAttemptRepository.deleteByQuizIdIn(quizIds);
        }
        lessonNoteRepository.deleteByEpisodeIdIn(episodeIds);
        lessonProgressRepository.deleteByEpisodeIdIn(episodeIds);
    }
}
