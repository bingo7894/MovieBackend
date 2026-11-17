package com.nerflix.clone.dao;

import com.nerflix.clone.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByEpisodeIdOrderByIdAsc(Long episodeId);

    List<Quiz> findByEpisodeIdAndPublishedTrueOrderByIdAsc(Long episodeId);

    @Query("SELECT q.id FROM Quiz q WHERE q.episode.id IN :episodeIds")
    List<Long> findQuizIdsByEpisodeIds(@Param("episodeIds") List<Long> episodeIds);
}
