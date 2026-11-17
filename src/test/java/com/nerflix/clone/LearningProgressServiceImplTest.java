package com.nerflix.clone;

import com.nerflix.clone.dao.EnrollmentRepository;
import com.nerflix.clone.dao.LessonNoteRepository;
import com.nerflix.clone.dao.LessonProgressRepository;
import com.nerflix.clone.dto.request.ProgressUpdateRequest;
import com.nerflix.clone.dto.response.LessonProgressResponse;
import com.nerflix.clone.entity.Episode;
import com.nerflix.clone.entity.LessonNote;
import com.nerflix.clone.entity.LessonProgress;
import com.nerflix.clone.entity.User;
import com.nerflix.clone.serviceimpl.LearningProgressServiceImpl;
import com.nerflix.clone.util.LmsDataHelper;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.ServiceUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningProgressServiceImplTest {

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LessonNoteRepository lessonNoteRepository;

    @Mock
    private ServiceUtils serviceUtils;

    @Mock
    private LmsMapper lmsMapper;

    @Mock
    private LmsDataHelper lmsDataHelper;

    @InjectMocks
    private LearningProgressServiceImpl progressService;

    @Test
    void updateProgressAutoCompletesAtNinetyPercentDuration() {
        User user = new User();
        user.setId(1L);
        user.setEmail("student@lms.com");

        Episode episode = new Episode();
        episode.setId(10L);
        episode.setDuration(100);

        when(serviceUtils.getUserByEmailOrThrow("student@lms.com")).thenReturn(user);
        when(serviceUtils.getEpisodeByIdOrThrow(10L)).thenReturn(episode);
        when(lessonProgressRepository.findByUserIdAndEpisodeId(1L, 10L)).thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setWatchedSeconds(92);

        LessonProgressResponse response = progressService.updateProgress(10L, request, "student@lms.com");

        assertTrue(response.isCompleted());
        assertEquals(92, response.getWatchedSeconds());
        assertNotNull(response.getCompletedAt());
    }

    @Test
    void deleteNoteThrowsAccessDeniedForDifferentUser() {
        User owner = new User();
        owner.setId(1L);
        User other = new User();
        other.setId(2L);
        other.setEmail("other@lms.com");

        LessonNote note = new LessonNote();
        note.setId(55L);
        note.setUser(owner);

        when(serviceUtils.getUserByEmailOrThrow("other@lms.com")).thenReturn(other);
        when(lessonNoteRepository.findById(55L)).thenReturn(Optional.of(note));

        assertThrows(AccessDeniedException.class, () -> progressService.deleteNote(55L, "other@lms.com"));
    }
}
