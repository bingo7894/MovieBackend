package com.nerflix.clone.dao;

import com.nerflix.clone.entity.LessonAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonAttachmentRepository extends JpaRepository<LessonAttachment, Long> {
    List<LessonAttachment> findByEpisodeIdOrderByIdAsc(Long episodeId);
}
