package com.nerflix.clone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DiscussionReplyRequest {

    @NotBlank(message = "Reply content is required")
    @Size(max = 4000, message = "Reply must not exceed 4000 characters")
    private String content;
}
