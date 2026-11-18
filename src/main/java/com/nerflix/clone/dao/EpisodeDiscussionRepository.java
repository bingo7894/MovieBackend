package com.nerflix.clone.dao;

import com.nerflix.clone.entity.EpisodeDiscussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EpisodeDiscussionRepository extends JpaRepository<EpisodeDiscussion, Long> {

    List<EpisodeDiscussion> findByEpisodeIdOrderByPinnedDescCreatedAtDesc(Long episodeId);

    @Modifying
    @Query("DELETE FROM EpisodeDiscussion d WHERE d.episode.id IN :episodeIds")
    void deleteByEpisodeIdIn(@Param("episodeIds") List<Long> episodeIds);

    @Query("SELECT d.id FROM EpisodeDiscussion d WHERE d.episode.id IN :episodeIds")
    List<Long> findIdsByEpisodeIdIn(@Param("episodeIds") List<Long> episodeIds);

    @Query("SELECT d.id FROM EpisodeDiscussion d WHERE d.user.id = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
