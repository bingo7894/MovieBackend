package com.nerflix.clone.dao;

import com.nerflix.clone.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserIdAndQuizIdOrderBySubmittedAtDesc(Long userId, Long quizId);

    List<QuizAttempt> findByUserIdAndQuizIdIn(Long userId, List<Long> quizIds);

    List<QuizAttempt> findByQuizIdIn(List<Long> quizIds);

    void deleteByQuizId(Long quizId);

    void deleteByQuizIdIn(List<Long> quizIds);

    void deleteByUserId(Long userId);
}
