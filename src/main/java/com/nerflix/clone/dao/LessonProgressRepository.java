package com.nerflix.clone.dao;

import com.nerflix.clone.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByUserIdAndEpisodeId(Long userId, Long episodeId);

    List<LessonProgress> findByUserIdAndEpisodeIdIn(Long userId, List<Long> episodeIds);

    void deleteByEpisodeId(Long episodeId);

    void deleteByEpisodeIdIn(List<Long> episodeIds);

    void deleteByUserId(Long userId);
}
