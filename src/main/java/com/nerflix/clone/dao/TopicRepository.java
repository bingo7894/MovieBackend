package com.nerflix.clone.dao;

import com.nerflix.clone.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByCourseIdOrderBySortOrderAscIdAsc(Long courseId);
}
