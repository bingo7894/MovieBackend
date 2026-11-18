package com.nerflix.clone.dao;

import com.nerflix.clone.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByUserIdAndEpisodeId(Long userId, Long episodeId);

    List<LessonProgress> findByUserIdAndEpisodeIdIn(Long userId, List<Long> episodeIds);

    List<LessonProgress> findByEpisodeIdIn(List<Long> episodeIds);

    @Query("SELECT lp.episode.topic.course.id FROM LessonProgress lp " +
            "WHERE lp.user.id = :userId AND lp.episode.topic.course.published = true " +
            "GROUP BY lp.episode.topic.course.id " +
            "ORDER BY MAX(lp.updatedAt) DESC")
    List<Long> findRecentCourseIdsByUserId(@Param("userId") Long userId);

    void deleteByEpisodeId(Long episodeId);

    void deleteByEpisodeIdIn(List<Long> episodeIds);

    void deleteByUserId(Long userId);
}
