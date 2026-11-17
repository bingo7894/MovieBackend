package com.nerflix.clone.dao;

import com.nerflix.clone.entity.LessonNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonNoteRepository extends JpaRepository<LessonNote, Long> {

    List<LessonNote> findByUserIdAndEpisodeIdOrderByTimestampSecondsAscCreatedAtDesc(Long userId, Long episodeId);

    void deleteByEpisodeId(Long episodeId);

    void deleteByEpisodeIdIn(List<Long> episodeIds);

    void deleteByUserId(Long userId);
}
