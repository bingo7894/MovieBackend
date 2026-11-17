package com.nerflix.clone.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReorderRequest {

    @NotEmpty(message = "orderedIds must not be empty")
    private List<Long> orderedIds;
}
