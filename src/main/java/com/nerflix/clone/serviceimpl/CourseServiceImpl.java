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

        return courses.stream()
                .map(c -> lmsMapper.toCourseSummary(c, enrolledIds.contains(c.getId()), progressMap, false))
                .toList();
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

        return lmsMapper.toCourseDetail(course, enrolled, progressMap, attemptsMap, false);
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
