package com.nerflix.clone.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nerflix.clone.dao.QuizAttemptRepository;
import com.nerflix.clone.dao.QuizRepository;
import com.nerflix.clone.dto.request.QuizOptionRequest;
import com.nerflix.clone.dto.request.QuizQuestionRequest;
import com.nerflix.clone.dto.request.QuizRequest;
import com.nerflix.clone.dto.request.QuizSubmitRequest;
import com.nerflix.clone.dto.response.*;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.enums.QuestionType;
import com.nerflix.clone.exception.ResourceNotFoundException;
import com.nerflix.clone.service.QuizService;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class QuizServiceImpl implements QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private LmsMapper lmsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public QuizDetailResponse createQuiz(Long episodeId, QuizRequest request) {
        Episode episode = serviceUtils.getEpisodeByIdOrThrow(episodeId);
        Quiz quiz = new Quiz();
        quiz.setEpisode(episode);
        applyQuizData(quiz, request);
        Quiz saved = quizRepository.save(quiz);
        return lmsMapper.toQuizDetail(saved, true);
    }

    @Override
    @Transactional
    public QuizDetailResponse updateQuiz(Long quizId, QuizRequest request) {
        Quiz quiz = serviceUtils.getQuizByIdOrThrow(quizId);
        applyQuizData(quiz, request);
        Quiz saved = quizRepository.save(quiz);
        return lmsMapper.toQuizDetail(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDetailResponse getQuizForAdmin(Long quizId) {
        Quiz quiz = serviceUtils.getQuizByIdOrThrow(quizId);
        return lmsMapper.toQuizDetail(quiz, true);
    }

    @Override
    @Transactional
    public MessageResponse deleteQuiz(Long quizId) {
        Quiz quiz = serviceUtils.getQuizByIdOrThrow(quizId);
        quizAttemptRepository.deleteByQuizId(quizId);
        quizRepository.delete(quiz);
        return new MessageResponse("Quiz deleted successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDetailResponse getQuizForLearner(Long quizId) {
        Quiz quiz = serviceUtils.getQuizByIdOrThrow(quizId);
        Episode episode = quiz.getEpisode();
        Course course = episode.getTopic().getCourse();
        if (!quiz.isPublished() || !episode.isPublished() || !course.isPublished()) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }
        return lmsMapper.toQuizDetail(quiz, false);
    }

    @Override
    @Transactional
    public QuizResultResponse submitQuiz(Long quizId, QuizSubmitRequest request, String email) {
        Quiz quiz = serviceUtils.getQuizByIdOrThrow(quizId);
        if (quiz.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("Quiz has no questions to grade.");
        }

        User user = serviceUtils.getUserByEmailOrThrow(email);
        Map<Long, List<Long>> submittedAnswers = request.getAnswers() != null ? request.getAnswers() : Map.of();

        int correctCount = 0;
        List<QuizResultResponse.QuestionResultItem> questionResults = new ArrayList<>();

        for (QuizQuestion question : quiz.getQuestions()) {
            List<Long> correctOptionIds = question.getOptions().stream()
                    .filter(QuizOption::isCorrect)
                    .map(QuizOption::getId)
                    .toList();

            List<Long> selectedOptionIds = submittedAnswers.getOrDefault(question.getId(), List.of());

            Set<Long> correctSet = new HashSet<>(correctOptionIds);
            Set<Long> selectedSet = new HashSet<>(selectedOptionIds);
            boolean isCorrect = !correctSet.isEmpty() && correctSet.equals(selectedSet);
            if (isCorrect) {
                correctCount++;
            }

            questionResults.add(new QuizResultResponse.QuestionResultItem(
                    question.getId(),
                    question.getQuestionText(),
                    selectedOptionIds,
                    correctOptionIds,
                    isCorrect,
                    question.getExplanation()
            ));
        }

        int totalQuestions = quiz.getQuestions().size();
        int scorePercent = Math.round((correctCount * 100.0f) / totalQuestions);
        int passingScore = quiz.getPassingScore() != null ? quiz.getPassingScore() : 70;
        boolean passed = scorePercent >= passingScore;

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setCorrectCount(correctCount);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setScorePercent(scorePercent);
        attempt.setPassed(passed);
        attempt.setAnswersJson(serializeAnswers(submittedAnswers));
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        return new QuizResultResponse(
                savedAttempt.getId(),
                quiz.getId(),
                quiz.getTitle(),
                scorePercent,
                correctCount,
                totalQuestions,
                passingScore,
                passed,
                savedAttempt.getSubmittedAt(),
                questionResults
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttemptSummaryResponse> getUserAttempts(Long quizId, String email) {
        serviceUtils.getQuizByIdOrThrow(quizId);
        User user = serviceUtils.getUserByEmailOrThrow(email);
        return quizAttemptRepository.findByUserIdAndQuizIdOrderBySubmittedAtDesc(user.getId(), quizId).stream()
                .map(a -> new QuizAttemptSummaryResponse(
                        a.getId(),
                        a.getScorePercent(),
                        a.getCorrectCount(),
                        a.getTotalQuestions(),
                        a.isPassed(),
                        a.getSubmittedAt()))
                .toList();
    }

    private void applyQuizData(Quiz quiz, QuizRequest request) {
        quiz.setTitle(request.getTitle().trim());
        quiz.setDescription(request.getDescription());
        int passing = request.getPassingScore() != null ? request.getPassingScore() : 70;
        if (passing < 0 || passing > 100) {
            throw new IllegalArgumentException("Passing score must be between 0 and 100");
        }
        quiz.setPassingScore(passing);
        int timeLimit = request.getTimeLimitMinutes() != null ? request.getTimeLimitMinutes() : 0;
        if (timeLimit < 0) {
            throw new IllegalArgumentException("Time limit must be 0 or positive");
        }
        quiz.setTimeLimitMinutes(timeLimit);
        if (request.getShuffleQuestions() != null) {
            quiz.setShuffleQuestions(request.getShuffleQuestions());
        }
        if (request.getPublished() != null) {
            quiz.setPublished(request.getPublished());
        }

        quiz.getQuestions().clear();
        if (request.getQuestions() == null) {
            return;
        }

        for (int i = 0; i < request.getQuestions().size(); i++) {
            QuizQuestionRequest qReq = request.getQuestions().get(i);
            QuizQuestion question = new QuizQuestion();
            question.setQuestionText(qReq.getQuestionText().trim());
            question.setExplanation(qReq.getExplanation());
            question.setSortOrder(qReq.getSortOrder() != null ? qReq.getSortOrder() : i + 1);
            question.setQuestionType(parseQuestionType(qReq.getQuestionType()));
            question.setQuiz(quiz);

            if (qReq.getOptions() == null || qReq.getOptions().size() < 2) {
                throw new IllegalArgumentException("Each question must have at least 2 options");
            }

            boolean hasCorrectOption = false;
            for (QuizOptionRequest optReq : qReq.getOptions()) {
                QuizOption option = new QuizOption();
                option.setOptionText(optReq.getOptionText().trim());
                option.setCorrect(optReq.isCorrect());
                option.setQuestion(question);
                question.getOptions().add(option);
                if (optReq.isCorrect()) {
                    hasCorrectOption = true;
                }
            }

            if (!hasCorrectOption) {
                throw new IllegalArgumentException(
                        "Question '" + qReq.getQuestionText() + "' must have at least one correct option");
            }

            quiz.getQuestions().add(question);
        }
    }

    private QuestionType parseQuestionType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return QuestionType.MULTIPLE_CHOICE;
        }
        try {
            return QuestionType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid question type: " + rawType);
        }
    }

    private String serializeAnswers(Map<Long, List<Long>> answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
