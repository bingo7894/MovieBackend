package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailValidationResponse {
    private boolean exists;
    private boolean available;
}
