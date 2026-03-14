package com.nerflix.clone.dao;

import com.nerflix.clone.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video,Long> {
}
