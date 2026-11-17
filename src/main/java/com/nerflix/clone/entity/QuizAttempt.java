package com.nerflix.clone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@NoArgsConstructor
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer scorePercent;

    @Column(nullable = false)
    private Integer correctCount;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private boolean passed;

    @Column(columnDefinition = "TEXT")
    private String answersJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant submittedAt;
}
