package com.nerflix.clone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nerflix.clone.dao.QuizAttemptRepository;
import com.nerflix.clone.dao.QuizRepository;
import com.nerflix.clone.dto.request.QuizSubmitRequest;
import com.nerflix.clone.dto.response.QuizDetailResponse;
import com.nerflix.clone.dto.response.QuizResultResponse;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.enums.QuestionType;
import com.nerflix.clone.serviceimpl.QuizServiceImpl;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.ServiceUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private ServiceUtils serviceUtils;

    @Spy
    private LmsMapper lmsMapper = new LmsMapper();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private QuizServiceImpl quizService;

    private Quiz buildSampleQuiz() {
        Course course = new Course();
        course.setId(1L);
        course.setPublished(true);

        Topic topic = new Topic();
        topic.setId(10L);
        topic.setCourse(course);

        Episode episode = new Episode();
        episode.setId(100L);
        episode.setPublished(true);
        episode.setTopic(topic);

        Quiz quiz = new Quiz();
        quiz.setId(50L);
        quiz.setTitle("Spring Boot Quiz");
        quiz.setPassingScore(50);
        quiz.setPublished(true);
        quiz.setEpisode(episode);

        QuizQuestion q1 = new QuizQuestion();
        q1.setId(1L);
        q1.setQuestionText("What annotation marks a REST controller?");
        q1.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        q1.setExplanation("@RestController combines @Controller and @ResponseBody.");
        QuizOption q1o1 = new QuizOption();
        q1o1.setId(11L);
        q1o1.setOptionText("@RestController");
        q1o1.setCorrect(true);
        q1o1.setQuestion(q1);
        QuizOption q1o2 = new QuizOption();
        q1o2.setId(12L);
        q1o2.setOptionText("@Service");
        q1o2.setCorrect(false);
        q1o2.setQuestion(q1);
        q1.setOptions(List.of(q1o1, q1o2));

        QuizQuestion q2 = new QuizQuestion();
        q2.setId(2L);
        q2.setQuestionText("Java 21 is an LTS release.");
        q2.setQuestionType(QuestionType.TRUE_FALSE);
        q2.setExplanation("Yes, Java 21 is LTS.");
        QuizOption q2o1 = new QuizOption();
        q2o1.setId(21L);
        q2o1.setOptionText("True");
        q2o1.setCorrect(true);
        q2o1.setQuestion(q2);
        QuizOption q2o2 = new QuizOption();
        q2o2.setId(22L);
        q2o2.setOptionText("False");
        q2o2.setCorrect(false);
        q2o2.setQuestion(q2);
        q2.setOptions(List.of(q2o1, q2o2));

        quiz.setQuestions(List.of(q1, q2));
        return quiz;
    }

    @Test
    void learnerQuizDoesNotLeakCorrectFlagsOrExplanations() {
        Quiz quiz = buildSampleQuiz();
        when(serviceUtils.getQuizByIdOrThrow(50L)).thenReturn(quiz);

        QuizDetailResponse response = quizService.getQuizForLearner(50L);

        assertEquals(2, response.getQuestions().size());
        assertNull(response.getQuestions().get(0).getExplanation());
        assertNull(response.getQuestions().get(0).getOptions().get(0).getCorrect());
    }

    @Test
    void submitQuizGradesAnswersAndPersistsAttempt() {
        Quiz quiz = buildSampleQuiz();
        User user = new User();
        user.setId(7L);
        user.setEmail("student@lms.com");

        when(serviceUtils.getQuizByIdOrThrow(50L)).thenReturn(quiz);
        when(serviceUtils.getUserByEmailOrThrow("student@lms.com")).thenReturn(user);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> {
            QuizAttempt attempt = inv.getArgument(0);
            attempt.setId(99L);
            return attempt;
        });

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setAnswers(Map.of(
                1L, List.of(11L),
                2L, List.of(22L)
        ));

        QuizResultResponse result = quizService.submitQuiz(50L, request, "student@lms.com");

        assertEquals(99L, result.getAttemptId());
        assertEquals(1, result.getCorrectCount());
        assertEquals(2, result.getTotalQuestions());
        assertEquals(50, result.getScorePercent());
        assertTrue(result.isPassed());
        assertEquals(2, result.getQuestionResults().size());
        assertTrue(result.getQuestionResults().get(0).isCorrect());
        assertFalse(result.getQuestionResults().get(1).isCorrect());
    }
}
