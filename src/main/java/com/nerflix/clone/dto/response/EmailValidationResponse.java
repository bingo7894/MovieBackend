package com.nerflix.clone.dto.response;

import lombok.Data;

@Data
public class EmailValidationResponse {
    private boolean exists;
    private boolean available;
}
