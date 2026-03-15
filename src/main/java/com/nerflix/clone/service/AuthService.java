package com.nerflix.clone.service;

import com.nerflix.clone.dto.request.UserRequest;
import com.nerflix.clone.dto.response.EmailValidationResponse;
import com.nerflix.clone.dto.response.LoginResponse;
import com.nerflix.clone.dto.response.MessageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public interface AuthService {
    MessageResponse signup(@Valid UserRequest userRequest);

    LoginResponse login(String email, String password);

    EmailValidationResponse validateEmail(String email);

    MessageResponse verifyEmail(String token);

    MessageResponse resendVerificationEmail(String email);

    MessageResponse forgotPassword(String email);

    MessageResponse resetPassword(String token,String newPassword);

    MessageResponse changePassword(String email,  String currentPassword,  String newPassword);

    LoginResponse currentUser(String email);
}
