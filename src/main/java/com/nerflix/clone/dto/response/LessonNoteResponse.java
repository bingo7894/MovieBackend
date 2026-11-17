package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonNoteResponse {
    private Long id;
    private Long episodeId;
    private Integer timestampSeconds;
    private String content;
    private Instant createdAt;
}
