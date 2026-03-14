package com.nerflix.clone.serviceimpl;

import com.nerflix.clone.dao.UserRepository;
import com.nerflix.clone.dto.request.UserRequest;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.entity.User;
import com.nerflix.clone.enums.Role;
import com.nerflix.clone.exception.EmailAlreadyExistsException;
import com.nerflix.clone.security.JwtUtil;
import com.nerflix.clone.service.AuthService;
import com.nerflix.clone.service.EmailService;
import com.nerflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private ServiceUtils serviceUtils;

    @Override
    public MessageResponse signup(UserRequest userRequest) {

        if(userRepository.existsByEmail(userRequest.getEmail())){
            throw  new EmailAlreadyExistsException("Email already exists.");
        }

        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setFullName(userRequest.getFullname());
        user.setRole(Role.USER);
        user.setActive(true);
        user.setEmailVerified(false);
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(86400));
        userRepository.save(user);
        emailService.sendVerificationEmail(userRequest.getEmail(),verificationToken);

        return new MessageResponse("Registration successful! Please check your email to verify your account");
    }
}
