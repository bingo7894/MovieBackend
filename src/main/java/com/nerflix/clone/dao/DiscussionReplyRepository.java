package com.nerflix.clone.dao;

import com.nerflix.clone.entity.DiscussionReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {

    @Modifying
    @Query("DELETE FROM DiscussionReply r WHERE r.discussion.id IN :discussionIds")
    void deleteByDiscussionIdIn(@Param("discussionIds") List<Long> discussionIds);

    void deleteByUserId(Long userId);
}
