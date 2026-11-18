package com.nerflix.clone.serviceimpl;

import com.nerflix.clone.dao.*;
import com.nerflix.clone.dto.response.AdminAnalyticsOverviewResponse;
import com.nerflix.clone.dto.response.CourseAnalyticsItemResponse;
import com.nerflix.clone.dto.response.CourseLearnerProgressResponse;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.enums.Role;
import com.nerflix.clone.service.AnalyticsService;
import com.nerflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private CourseCertificateRepository certificateRepository;

    @Autowired
    private CourseReviewRepository reviewRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Override
    @Transactional(readOnly = true)
    public AdminAnalyticsOverviewResponse getOverview() {
        long totalLearners = userRepository.countByRole(Role.USER);
        long totalEnrollments = enrollmentRepository.count();
        long totalCertificates = certificateRepository.count();

        List<QuizAttempt> allAttempts = quizAttemptRepository.findAll();
        long totalQuizAttempts = allAttempts.size();
        long passedAttempts = allAttempts.stream().filter(QuizAttempt::isPassed).count();
        int avgQuizPassRate = totalQuizAttempts > 0
                ? Math.round((passedAttempts * 100.0f) / totalQuizAttempts)
                : 0;

        List<CourseAnalyticsItemResponse> courseItems = courseRepository.findAll().stream()
                .map(this::buildCourseAnalyticsItem)
                .toList();

        return new AdminAnalyticsOverviewResponse(
                totalLearners,
                totalEnrollments,
                totalCertificates,
                totalQuizAttempts,
                avgQuizPassRate,
                courseItems
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseLearnerProgressResponse> getCourseLearners(Long courseId) {
        Course course = serviceUtils.getCourseByIdOrThrow(courseId);

        List<Episode> publishedEpisodes = course.getTopics().stream()
                .flatMap(t -> t.getEpisodes().stream())
                .filter(Episode::isPublished)
                .toList();
        List<Long> episodeIds = publishedEpisodes.stream().map(Episode::getId).toList();
        int totalEpisodes = publishedEpisodes.size();

        List<Long> publishedQuizIds = publishedEpisodes.stream()
                .flatMap(e -> e.getQuizzes().stream())
                .filter(Quiz::isPublished)
                .map(Quiz::getId)
                .toList();
        int totalPublishedQuizzes = publishedQuizIds.size();

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<LessonProgress> progresses = episodeIds.isEmpty()
                ? List.of()
                : lessonProgressRepository.findByEpisodeIdIn(episodeIds);
        List<QuizAttempt> attempts = publishedQuizIds.isEmpty()
                ? List.of()
                : quizAttemptRepository.findByQuizIdIn(publishedQuizIds);
        List<CourseCertificate> certs = certificateRepository.findByCourseId(courseId);

        Map<Long, Enrollment> enrollmentByUser = enrollments.stream()
                .collect(Collectors.toMap(e -> e.getUser().getId(), e -> e, (a, b) -> a));
        Map<Long, List<LessonProgress>> progressByUser = progresses.stream()
                .collect(Collectors.groupingBy(lp -> lp.getUser().getId()));
        Map<Long, List<QuizAttempt>> attemptsByUser = attempts.stream()
                .collect(Collectors.groupingBy(qa -> qa.getUser().getId()));
        Map<Long, CourseCertificate> certByUser = certs.stream()
                .collect(Collectors.toMap(c -> c.getUser().getId(), c -> c, (a, b) -> a));

        Map<Long, User> learnersMap = new LinkedHashMap<>();
        enrollments.forEach(e -> learnersMap.put(e.getUser().getId(), e.getUser()));
        progresses.forEach(lp -> learnersMap.put(lp.getUser().getId(), lp.getUser()));
        attempts.forEach(a -> learnersMap.put(a.getUser().getId(), a.getUser()));
        certs.forEach(c -> learnersMap.put(c.getUser().getId(), c.getUser()));

        List<CourseLearnerProgressResponse> rows = new ArrayList<>();
        for (User learner : learnersMap.values()) {
            Long uid = learner.getId();
            Enrollment enr = enrollmentByUser.get(uid);
            List<LessonProgress> userProgress = progressByUser.getOrDefault(uid, List.of());
            List<QuizAttempt> userAttempts = attemptsByUser.getOrDefault(uid, List.of());
            CourseCertificate cert = certByUser.get(uid);

            int completedCount = (int) userProgress.stream().filter(LessonProgress::isCompleted).count();
            int progressPct = totalEpisodes > 0 ? Math.min(100, Math.round((completedCount * 100.0f) / totalEpisodes)) : 0;
            long watchedSec = userProgress.stream()
                    .mapToLong(lp -> lp.getWatchedSeconds() != null ? lp.getWatchedSeconds() : 0L)
                    .sum();

            int passedQuizzesCount = (int) userAttempts.stream()
                    .filter(QuizAttempt::isPassed)
                    .map(a -> a.getQuiz().getId())
                    .distinct()
                    .count();
            Integer avgScore = userAttempts.isEmpty()
                    ? null
                    : (int) Math.round(userAttempts.stream().mapToInt(QuizAttempt::getScorePercent).average().orElse(0));

            Instant lastActivity = resolveLastActivity(enr, userProgress, userAttempts);

            rows.add(new CourseLearnerProgressResponse(
                    uid,
                    learner.getFullName(),
                    learner.getEmail(),
                    enr != null,
                    enr != null ? enr.getEnrolledAt() : null,
                    completedCount,
                    totalEpisodes,
                    progressPct,
                    watchedSec,
                    userAttempts.size(),
                    passedQuizzesCount,
                    totalPublishedQuizzes,
                    avgScore,
                    cert != null ? cert.getCertificateCode() : null,
                    lastActivity
            ));
        }

        rows.sort(Comparator.comparingInt(CourseLearnerProgressResponse::getProgressPercent).reversed()
                .thenComparing(CourseLearnerProgressResponse::getLastActivityAt,
                        Comparator.nullsLast(Comparator.reverseOrder())));
        return rows;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCourseLearnersCsv(Long courseId) {
        List<CourseLearnerProgressResponse> rows = getCourseLearners(courseId);
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // UTF-8 BOM for Excel Thai support
        sb.append("Full Name,Email,Enrolled,Completed EPs,Total EPs,Progress %,Watched Seconds,Quizzes Passed,Total Quizzes,Avg Quiz Score,Certificate Code,Last Activity\n");
        for (CourseLearnerProgressResponse r : rows) {
            sb.append(escapeCsv(r.getFullName())).append(',')
              .append(escapeCsv(r.getEmail())).append(',')
              .append(r.isEnrolled() ? "Yes" : "No").append(',')
              .append(r.getCompletedEpisodes()).append(',')
              .append(r.getTotalEpisodes()).append(',')
              .append(r.getProgressPercent()).append(',')
              .append(r.getTotalWatchedSeconds()).append(',')
              .append(r.getQuizzesPassedCount()).append(',')
              .append(r.getTotalPublishedQuizzes()).append(',')
              .append(r.getAverageQuizScore() != null ? r.getAverageQuizScore() : "").append(',')
              .append(escapeCsv(r.getCertificateCode())).append(',')
              .append(r.getLastActivityAt() != null ? escapeCsv(r.getLastActivityAt().toString()) : "")
              .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private CourseAnalyticsItemResponse buildCourseAnalyticsItem(Course course) {
        Long courseId = course.getId();
        List<Episode> episodes = course.getTopics().stream()
                .flatMap(t -> t.getEpisodes().stream())
                .filter(Episode::isPublished)
                .toList();
        List<Long> episodeIds = episodes.stream().map(Episode::getId).toList();
        int totalEps = episodes.size();

        List<Long> quizIds = episodes.stream()
                .flatMap(e -> e.getQuizzes().stream())
                .map(Quiz::getId)
                .toList();

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<LessonProgress> progresses = episodeIds.isEmpty()
                ? List.of()
                : lessonProgressRepository.findByEpisodeIdIn(episodeIds);
        List<QuizAttempt> attempts = quizIds.isEmpty()
                ? List.of()
                : quizAttemptRepository.findByQuizIdIn(quizIds);

        Set<Long> activeUsers = new HashSet<>();
        enrollments.forEach(e -> activeUsers.add(e.getUser().getId()));
        progresses.forEach(lp -> activeUsers.add(lp.getUser().getId()));
        attempts.forEach(a -> activeUsers.add(a.getUser().getId()));

        Map<Long, Long> completedByUser = progresses.stream()
                .filter(LessonProgress::isCompleted)
                .collect(Collectors.groupingBy(lp -> lp.getUser().getId(), Collectors.counting()));

        int completedLearners = 0;
        long totalPctSum = 0;
        if (totalEps > 0 && !activeUsers.isEmpty()) {
            for (Long uid : activeUsers) {
                long done = completedByUser.getOrDefault(uid, 0L);
                int pct = Math.min(100, Math.round((done * 100.0f) / totalEps));
                totalPctSum += pct;
                if (done >= totalEps) {
                    completedLearners++;
                }
            }
        }
        int avgProgress = activeUsers.isEmpty() ? 0 : (int) Math.round((double) totalPctSum / activeUsers.size());
        int avgQuizScore = attempts.isEmpty()
                ? 0
                : (int) Math.round(attempts.stream().mapToInt(QuizAttempt::getScorePercent).average().orElse(0));

        long certCount = certificateRepository.countByCourseId(courseId);
        Double rawAvg = reviewRepository.getAverageRatingForCourse(courseId);
        double avgRating = rawAvg != null ? Math.round(rawAvg * 10.0) / 10.0 : 0.0;
        long reviewCount = reviewRepository.countByCourseId(courseId);

        return new CourseAnalyticsItemResponse(
                courseId,
                course.getTitle(),
                course.getCategory(),
                course.getLevel() != null ? course.getLevel().name() : null,
                course.isPublished(),
                course.getTopics().size(),
                totalEps,
                enrollments.size(),
                activeUsers.size(),
                completedLearners,
                avgProgress,
                certCount,
                attempts.size(),
                avgQuizScore,
                avgRating,
                reviewCount
        );
    }

    private Instant resolveLastActivity(
            Enrollment enrollment,
            List<LessonProgress> progresses,
            List<QuizAttempt> attempts) {
        Instant latest = enrollment != null ? enrollment.getEnrolledAt() : null;
        for (LessonProgress lp : progresses) {
            if (lp.getUpdatedAt() != null && (latest == null || lp.getUpdatedAt().isAfter(latest))) {
                latest = lp.getUpdatedAt();
            }
        }
        for (QuizAttempt qa : attempts) {
            if (qa.getSubmittedAt() != null && (latest == null || qa.getSubmittedAt().isAfter(latest))) {
                latest = qa.getSubmittedAt();
            }
        }
        return latest;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
