package com.nerflix.clone.dao;

import com.nerflix.clone.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    List<Episode> findByTopicIdOrderByEpisodeNumberAscIdAsc(Long topicId);

    @Query("SELECT COALESCE(SUM(e.duration), 0) FROM Episode e")
    long getTotalDuration();

    @Query("SELECT e.id FROM Episode e WHERE e.topic.id = :topicId")
    List<Long> findEpisodeIdsByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT e.id FROM Episode e WHERE e.topic.course.id = :courseId")
    List<Long> findAllEpisodeIdsByCourseId(@Param("courseId") Long courseId);
}
