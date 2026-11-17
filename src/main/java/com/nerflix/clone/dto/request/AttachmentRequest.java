package com.nerflix.clone.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AttachmentRequest {

    @NotBlank(message = "Attachment title is required")
    private String title;

    @NotBlank(message = "Original filename is required")
    private String originalFilename;

    @NotBlank(message = "File UUID is required")
    private String fileUuid;

    private Long fileSize;

    private String contentType;
}
