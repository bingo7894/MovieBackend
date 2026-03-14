package com.nerflix.clone.service;

import com.nerflix.clone.dto.request.UserRequest;
import com.nerflix.clone.dto.response.MessageResponse;
import jakarta.validation.Valid;

public interface AuthService {
    MessageResponse signup(@Valid UserRequest userRequest);
}
