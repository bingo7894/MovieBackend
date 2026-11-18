package com.nerflix.clone;

import com.nerflix.clone.dao.*;
import com.nerflix.clone.dto.response.CourseLearnerProgressResponse;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.serviceimpl.AnalyticsServiceImpl;
import com.nerflix.clone.util.ServiceUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private LessonProgressRepository lessonProgressRepository;
    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private CourseCertificateRepository certificateRepository;
    @Mock
    private CourseReviewRepository reviewRepository;
    @Mock
    private ServiceUtils serviceUtils;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    void aggregatesCourseLearnerProgressAndExportsCsv() {
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Spring Security");
        Topic topic = new Topic();
        topic.setId(10L);
        topic.setCourse(course);
        Episode ep1 = new Episode();
        ep1.setId(101L);
        ep1.setPublished(true);
        ep1.setTopic(topic);
        Episode ep2 = new Episode();
        ep2.setId(102L);
        ep2.setPublished(true);
        ep2.setTopic(topic);
        topic.getEpisodes().addAll(List.of(ep1, ep2));
        course.getTopics().add(topic);

        User student = new User();
        student.setId(5L);
        student.setFullName("Alice Learner");
        student.setEmail("alice@lms.com");

        Enrollment enr = new Enrollment();
        enr.setCourse(course);
        enr.setUser(student);

        LessonProgress lp1 = new LessonProgress();
        lp1.setUser(student);
        lp1.setEpisode(ep1);
        lp1.setCompleted(true);
        lp1.setWatchedSeconds(95);

        when(serviceUtils.getCourseByIdOrThrow(1L)).thenReturn(course);
        when(enrollmentRepository.findByCourseId(1L)).thenReturn(List.of(enr));
        when(lessonProgressRepository.findByEpisodeIdIn(List.of(101L, 102L))).thenReturn(List.of(lp1));
        when(certificateRepository.findByCourseId(1L)).thenReturn(List.of());

        List<CourseLearnerProgressResponse> rows = analyticsService.getCourseLearners(1L);
        assertEquals(1, rows.size());
        assertEquals(50, rows.get(0).getProgressPercent());
        assertEquals(1, rows.get(0).getCompletedEpisodes());
        assertEquals(95, rows.get(0).getTotalWatchedSeconds());

        String csv = new String(analyticsService.exportCourseLearnersCsv(1L), StandardCharsets.UTF_8);
        assertTrue(csv.contains("Alice Learner"));
        assertTrue(csv.contains("alice@lms.com"));
        assertTrue(csv.contains(",1,2,50,95,"));
    }
}
