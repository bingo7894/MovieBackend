package com.nerflix.clone;

import com.nerflix.clone.dao.CourseCertificateRepository;
import com.nerflix.clone.dao.LessonProgressRepository;
import com.nerflix.clone.dao.QuizAttemptRepository;
import com.nerflix.clone.dto.response.CertificateResponse;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.serviceimpl.CertificateServiceImpl;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.ServiceUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {

    @Mock
    private CourseCertificateRepository certificateRepository;

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private ServiceUtils serviceUtils;

    @Spy
    private LmsMapper lmsMapper = new LmsMapper();

    @InjectMocks
    private CertificateServiceImpl certificateService;

    private Course buildCourseWithOneEpAndQuiz() {
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Spring Boot Mastery");
        course.setPublished(true);

        Topic topic = new Topic();
        topic.setId(10L);
        topic.setCourse(course);

        Episode ep = new Episode();
        ep.setId(100L);
        ep.setPublished(true);
        ep.setDuration(120);
        ep.setTopic(topic);

        Quiz quiz = new Quiz();
        quiz.setId(200L);
        quiz.setPublished(true);
        quiz.setEpisode(ep);
        ep.getQuizzes().add(quiz);

        topic.getEpisodes().add(ep);
        course.getTopics().add(topic);
        return course;
    }

    @Test
    void issueCertificateFailsWhenQuizzesNotPassed() {
        Course course = buildCourseWithOneEpAndQuiz();
        User user = new User();
        user.setId(5L);
        user.setEmail("student@lms.com");
        user.setFullName("Student Test");

        Episode ep = course.getTopics().get(0).getEpisodes().get(0);
        LessonProgress lp = new LessonProgress();
        lp.setEpisode(ep);
        lp.setCompleted(true);

        when(serviceUtils.getCourseByIdOrThrow(1L)).thenReturn(course);
        when(serviceUtils.getUserByEmailOrThrow("student@lms.com")).thenReturn(user);
        when(certificateRepository.findByUserIdAndCourseId(5L, 1L)).thenReturn(Optional.empty());
        when(lessonProgressRepository.findByUserIdAndEpisodeIdIn(5L, List.of(100L))).thenReturn(List.of(lp));
        when(quizAttemptRepository.findByUserIdAndQuizIdIn(5L, List.of(200L))).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> certificateService.issueOrGetCertificate(1L, "student@lms.com"));
        assertTrue(ex.getMessage().contains("Pass all course exercises"));
    }

    @Test
    void issueCertificateSucceedsWhenEpisodesCompletedAndQuizzesPassed() {
        Course course = buildCourseWithOneEpAndQuiz();
        User user = new User();
        user.setId(5L);
        user.setEmail("student@lms.com");
        user.setFullName("Student Test");

        Episode ep = course.getTopics().get(0).getEpisodes().get(0);
        LessonProgress lp = new LessonProgress();
        lp.setEpisode(ep);
        lp.setCompleted(true);

        Quiz quiz = ep.getQuizzes().get(0);
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setPassed(true);

        when(serviceUtils.getCourseByIdOrThrow(1L)).thenReturn(course);
        when(serviceUtils.getUserByEmailOrThrow("student@lms.com")).thenReturn(user);
        when(certificateRepository.findByUserIdAndCourseId(5L, 1L)).thenReturn(Optional.empty());
        when(lessonProgressRepository.findByUserIdAndEpisodeIdIn(5L, List.of(100L))).thenReturn(List.of(lp));
        when(quizAttemptRepository.findByUserIdAndQuizIdIn(5L, List.of(200L))).thenReturn(List.of(attempt));
        when(certificateRepository.findByCertificateCode(any())).thenReturn(Optional.empty());
        when(certificateRepository.save(any(CourseCertificate.class))).thenAnswer(inv -> {
            CourseCertificate c = inv.getArgument(0);
            c.setId(77L);
            return c;
        });

        CertificateResponse resp = certificateService.issueOrGetCertificate(1L, "student@lms.com");

        assertNotNull(resp.getCertificateCode());
        assertTrue(resp.getCertificateCode().startsWith("PL-"));
        assertEquals("Student Test", resp.getRecipientName());
        assertEquals("Spring Boot Mastery", resp.getCourseTitle());
        assertTrue(resp.isValid());
    }
}
