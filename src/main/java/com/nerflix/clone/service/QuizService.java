package com.nerflix.clone.service;

import com.nerflix.clone.dto.request.QuizRequest;
import com.nerflix.clone.dto.request.QuizSubmitRequest;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.QuizAttemptSummaryResponse;
import com.nerflix.clone.dto.response.QuizDetailResponse;
import com.nerflix.clone.dto.response.QuizResultResponse;

import java.util.List;

public interface QuizService {
    QuizDetailResponse createQuiz(Long episodeId, QuizRequest request);

    QuizDetailResponse updateQuiz(Long quizId, QuizRequest request);

    QuizDetailResponse getQuizForAdmin(Long quizId);

    MessageResponse deleteQuiz(Long quizId);

    QuizDetailResponse getQuizForLearner(Long quizId);

    QuizResultResponse submitQuiz(Long quizId, QuizSubmitRequest request, String email);

    List<QuizAttemptSummaryResponse> getUserAttempts(Long quizId, String email);
}
