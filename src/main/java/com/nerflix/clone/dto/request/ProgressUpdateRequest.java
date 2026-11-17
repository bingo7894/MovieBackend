package com.nerflix.clone.dto.request;

import lombok.Data;

@Data
public class ProgressUpdateRequest {
    private Integer watchedSeconds;
    private Boolean completed;
}
