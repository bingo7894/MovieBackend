package com.nerflix.clone.serviceimpl;

import com.nerflix.clone.dao.*;
import com.nerflix.clone.dto.request.CourseRequest;
import com.nerflix.clone.dto.response.*;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.enums.CourseLevel;
import com.nerflix.clone.exception.ResourceNotFoundException;
import com.nerflix.clone.service.CourseService;
import com.nerflix.clone.util.LmsDataHelper;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.PaginationUtils;
import com.nerflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private CourseCertificateRepository courseCertificateRepository;

    @Autowired
    private CourseReviewRepository courseReviewRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private LmsMapper lmsMapper;

    @Autowired
    private LmsDataHelper lmsDataHelper;

    @Override
    @Transactional
    public CourseSummaryResponse createCourse(CourseRequest request) {
        Course course = new Course();
        applyCourseFields(course, request);
        if (request.getPublished() != null) {
            course.setPublished(request.getPublished());
        }
        Course saved = courseRepository.save(course);
        return lmsMapper.toCourseSummary(saved, false, Map.of(), true);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getAllAdminCourses(int page, int size, String search) {
        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");
        Page<Course> coursePage = (search != null && !search.trim().isEmpty())
                ? courseRepository.searchAllCourses(search.trim(), pageable)
                : courseRepository.findAll(pageable);

        List<CourseSummaryResponse> content = coursePage.getContent().stream()
                .map(c -> lmsMapper.toCourseSummary(c, false, Map.of(), true))
                .toList();
        lmsDataHelper.enrichCourseSummaryStats(content);
        return PaginationUtils.toPageResponse(coursePage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetailForAdmin(Long courseId) {
        Course course = serviceUtils.getCourseByIdOrThrow(courseId);
        return lmsMapper.toCourseDetail(course, false, Map.of(), Map.of(), true);
    }

    @Override
    @Transactional
    public CourseSummaryResponse updateCourse(Long id, CourseRequest request) {
        Course course = serviceUtils.getCourseByIdOrThrow(id);
        applyCourseFields(course, request);
        if (request.getPublished() != null) {
            course.setPublished(request.getPublished());
        }
        Course saved = courseRepository.save(course);
        return lmsMapper.toCourseSummary(saved, false, Map.of(), true);
    }

    @Override
    @Transactional
    public MessageResponse deleteCourse(Long id) {
        Course course = serviceUtils.getCourseByIdOrThrow(id);
        List<Long> episodeIds = episodeRepository.findAllEpisodeIdsByCourseId(id);
        lmsDataHelper.deleteEpisodeDependencies(episodeIds);
        courseCertificateRepository.deleteByCourseId(id);
        courseReviewRepository.deleteByCourseId(id);
        enrollmentRepository.deleteByCourseId(id);
        courseRepository.delete(course);
        return new MessageResponse("Course deleted successfully.");
    }

    @Override
    @Transactional
    public MessageResponse toggleCoursePublish(Long id, boolean published) {
        Course course = serviceUtils.getCourseByIdOrThrow(id);
        course.setPublished(published);
        courseRepository.save(course);
        return new MessageResponse("Course publish status updated successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public CourseStatsResponse getAdminStats() {
        long totalCourses = courseRepository.count();
        long publishedCourses = courseRepository.countByPublishedTrue();
        long totalTopics = topicRepository.count();
        long totalEpisodes = episodeRepository.count();
        long totalDuration = episodeRepository.getTotalDuration();
        long totalQuizzes = quizRepository.count();
        return new CourseStatsResponse(
                totalCourses, publishedCourses, totalTopics, totalEpisodes, totalDuration, totalQuizzes);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getPublishedCourses(
            int page, int size, String search, String category, String email) {

        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty();

        Page<Course> coursePage;
        if (hasCategory && hasSearch) {
            coursePage = courseRepository.searchPublishedCoursesByCategory(category.trim(), search.trim(), pageable);
        } else if (hasCategory) {
            coursePage = courseRepository.findPublishedCoursesByCategory(category.trim(), pageable);
        } else if (hasSearch) {
            coursePage = courseRepository.searchPublishedCourses(search.trim(), pageable);
        } else {
            coursePage = courseRepository.findPublishedCourses(pageable);
        }

        User user = serviceUtils.getUserByEmailOrThrow(email);
        List<Course> courses = coursePage.getContent();
        Set<Long> enrolledIds = lmsDataHelper.loadEnrolledCourseIds(user.getId(), courses);
        Map<Long, LessonProgress> progressMap = lmsDataHelper.loadProgressMapForCourses(user.getId(), courses);

        List<CourseSummaryResponse> responses = courses.stream()
                .map(c -> lmsMapper.toCourseSummary(c, enrolledIds.contains(c.getId()), progressMap, false))
                .toList();
        lmsDataHelper.enrichCourseSummaryStats(responses);
        return PaginationUtils.toPageResponse(coursePage, responses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getFeaturedCourses(String email) {
        Pageable pageable = PageRequest.of(0, 5);
        List<Course> courses = courseRepository.findFeaturedCourses(pageable);
        User user = serviceUtils.getUserByEmailOrThrow(email);
        Set<Long> enrolledIds = lmsDataHelper.loadEnrolledCourseIds(user.getId(), courses);
        Map<Long, LessonProgress> progressMap = lmsDataHelper.loadProgressMapForCourses(user.getId(), courses);

        List<CourseSummaryResponse> responses = courses.stream()
                .map(c -> lmsMapper.toCourseSummary(c, enrolledIds.contains(c.getId()), progressMap, false))
                .toList();
        lmsDataHelper.enrichCourseSummaryStats(responses);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getInProgressCourses(String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        List<Long> recentCourseIds = lessonProgressRepository.findRecentCourseIdsByUserId(user.getId());
        if (recentCourseIds.isEmpty()) {
            return List.of();
        }
        List<Long> limitedIds = recentCourseIds.stream().limit(6).toList();
        Map<Long, Course> byId = new HashMap<>();
        for (Course c : courseRepository.findAllById(limitedIds)) {
            if (c.isPublished()) {
                byId.put(c.getId(), c);
            }
        }
        List<Course> orderedCourses = limitedIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();
        if (orderedCourses.isEmpty()) {
            return List.of();
        }
        Set<Long> enrolledIds = lmsDataHelper.loadEnrolledCourseIds(user.getId(), orderedCourses);
        Map<Long, LessonProgress> progressMap = lmsDataHelper.loadProgressMapForCourses(user.getId(), orderedCourses);
        List<CourseSummaryResponse> responses = orderedCourses.stream()
                .map(c -> lmsMapper.toCourseSummary(c, enrolledIds.contains(c.getId()), progressMap, false))
                .filter(c -> c.getEpisodeCount() > 0 && c.getCompletedEpisodes() < c.getEpisodeCount())
                .toList();
        lmsDataHelper.enrichCourseSummaryStats(responses);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetailForLearner(Long courseId, String email) {
        Course course = serviceUtils.getCourseByIdOrThrow(courseId);
        if (!course.isPublished()) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        User user = serviceUtils.getUserByEmailOrThrow(email);
        boolean enrolled = enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId);
        List<Long> episodeIds = course.getTopics().stream()
                .flatMap(t -> t.getEpisodes().stream())
                .map(Episode::getId)
                .toList();
        Map<Long, LessonProgress> progressMap = lmsDataHelper.loadProgressMapForEpisodeIds(user.getId(), episodeIds);
        Map<Long, List<QuizAttempt>> attemptsMap = lmsDataHelper.loadAttemptsMapForCourse(user.getId(), course);

        CourseDetailResponse detail = lmsMapper.toCourseDetail(course, enrolled, progressMap, attemptsMap, false);

        Double avg = courseReviewRepository.getAverageRatingForCourse(courseId);
        detail.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        detail.setReviewCount(courseReviewRepository.countByCourseId(courseId));
        detail.setEnrollmentCount(enrollmentRepository.countByCourseId(courseId));

        boolean allQuizzesPassed = detail.getTopics().stream()
                .flatMap(t -> t.getEpisodes().stream())
                .flatMap(e -> e.getQuizzes().stream())
                .allMatch(q -> Boolean.TRUE.equals(q.getPassed()));
        detail.setAllQuizzesPassed(allQuizzesPassed);
        detail.setCertificateEligible(detail.getEpisodeCount() > 0 && detail.getProgressPercent() >= 100 && allQuizzesPassed);
        courseCertificateRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .ifPresent(cert -> detail.setCertificateCode(cert.getCertificateCode()));

        return detail;
    }

    private void applyCourseFields(Course course, CourseRequest request) {
        if (request.getTitle() != null) {
            course.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getThumbnailUuid() != null) {
            course.setThumbnailUuid(serviceUtils.extractUuid(request.getThumbnailUuid()));
        }
        if (request.getCategory() != null) {
            course.setCategory(request.getCategory().trim());
        }
        if (request.getLevel() != null && !request.getLevel().isBlank()) {
            try {
                course.setLevel(CourseLevel.valueOf(request.getLevel().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid course level: " + request.getLevel());
            }
        }
    }
}
