package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String title;
    private String originalFilename;
    private String fileUuid;
    private String fileUrl;
    private String downloadUrl;
    private Long fileSize;
    private String contentType;
    private Long episodeId;
    private Instant createdAt;
}
