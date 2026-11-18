package com.nerflix.clone.serviceimpl;

import com.nerflix.clone.dao.EnrollmentRepository;
import com.nerflix.clone.dao.LessonNoteRepository;
import com.nerflix.clone.dao.LessonProgressRepository;
import com.nerflix.clone.dto.request.LessonNoteRequest;
import com.nerflix.clone.dto.request.ProgressUpdateRequest;
import com.nerflix.clone.dto.response.*;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.exception.ResourceNotFoundException;
import com.nerflix.clone.service.LearningProgressService;
import com.nerflix.clone.util.LmsDataHelper;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.PaginationUtils;
import com.nerflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class LearningProgressServiceImpl implements LearningProgressService {

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonNoteRepository lessonNoteRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private LmsMapper lmsMapper;

    @Autowired
    private LmsDataHelper lmsDataHelper;

    @Override
    @Transactional
    public LessonProgressResponse updateProgress(Long episodeId, ProgressUpdateRequest request, String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        Episode episode = serviceUtils.getEpisodeByIdOrThrow(episodeId);

        LessonProgress progress = lessonProgressRepository
                .findByUserIdAndEpisodeId(user.getId(), episodeId)
                .orElseGet(() -> {
                    LessonProgress lp = new LessonProgress();
                    lp.setUser(user);
                    lp.setEpisode(episode);
                    return lp;
                });

        if (request.getWatchedSeconds() != null && request.getWatchedSeconds() >= 0) {
            progress.setWatchedSeconds(request.getWatchedSeconds());
        }

        boolean shouldComplete = progress.isCompleted();
        if (request.getCompleted() != null) {
            shouldComplete = request.getCompleted();
        } else if (episode.getDuration() != null && episode.getDuration() > 0 && progress.getWatchedSeconds() != null) {
            if (progress.getWatchedSeconds() >= episode.getDuration() * 0.9) {
                shouldComplete = true;
            }
        }

        if (shouldComplete && !progress.isCompleted()) {
            progress.setCompletedAt(Instant.now());
        } else if (!shouldComplete) {
            progress.setCompletedAt(null);
        }
        progress.setCompleted(shouldComplete);

        LessonProgress saved = lessonProgressRepository.save(progress);
        return new LessonProgressResponse(
                episodeId,
                saved.getWatchedSeconds(),
                saved.isCompleted(),
                saved.getCompletedAt());
    }

    @Override
    @Transactional
    public MessageResponse enrollCourse(Long courseId, String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        Course course = serviceUtils.getCourseByIdOrThrow(courseId);
        if (!course.isPublished()) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        if (!enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setUser(user);
            enrollment.setCourse(course);
            enrollmentRepository.save(enrollment);
        }
        return new MessageResponse("Enrolled in course successfully.");
    }

    @Override
    @Transactional
    public MessageResponse unenrollCourse(Long courseId, String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .ifPresent(enrollmentRepository::delete);
        return new MessageResponse("Removed course from My Learning.");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getEnrolledCourses(String email, int page, int size, String search) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        Pageable pageable = PaginationUtils.createPageRequest(page, size);
        Page<Enrollment> enrollmentPage = (search != null && !search.trim().isEmpty())
                ? enrollmentRepository.searchPublishedByUserId(user.getId(), search.trim(), pageable)
                : enrollmentRepository.findPublishedByUserId(user.getId(), pageable);

        List<Course> courses = enrollmentPage.getContent().stream()
                .map(Enrollment::getCourse)
                .toList();
        Map<Long, LessonProgress> progressMap = lmsDataHelper.loadProgressMapForCourses(user.getId(), courses);

        List<CourseSummaryResponse> responses = courses.stream()
                .map(c -> lmsMapper.toCourseSummary(c, true, progressMap, false))
                .toList();
        lmsDataHelper.enrichCourseSummaryStats(responses);
        return PaginationUtils.toPageResponse(enrollmentPage, responses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonNoteResponse> getNotes(Long episodeId, String email) {
        serviceUtils.getEpisodeByIdOrThrow(episodeId);
        User user = serviceUtils.getUserByEmailOrThrow(email);
        return lessonNoteRepository
                .findByUserIdAndEpisodeIdOrderByTimestampSecondsAscCreatedAtDesc(user.getId(), episodeId)
                .stream()
                .map(lmsMapper::toNoteResponse)
                .toList();
    }

    @Override
    @Transactional
    public LessonNoteResponse createNote(Long episodeId, LessonNoteRequest request, String email) {
        Episode episode = serviceUtils.getEpisodeByIdOrThrow(episodeId);
        User user = serviceUtils.getUserByEmailOrThrow(email);

        LessonNote note = new LessonNote();
        note.setEpisode(episode);
        note.setUser(user);
        note.setTimestampSeconds(request.getTimestampSeconds() != null && request.getTimestampSeconds() >= 0
                ? request.getTimestampSeconds()
                : 0);
        note.setContent(request.getContent().trim());

        LessonNote saved = lessonNoteRepository.save(note);
        return lmsMapper.toNoteResponse(saved);
    }

    @Override
    @Transactional
    public MessageResponse deleteNote(Long noteId, String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        LessonNote note = lessonNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));
        if (!note.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You cannot delete another user's note");
        }
        lessonNoteRepository.delete(note);
        return new MessageResponse("Note deleted successfully.");
    }
}
