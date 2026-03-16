package com.nerflix.clone.service;

import com.nerflix.clone.dto.request.UserRequest;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.PageResponse;
import com.nerflix.clone.dto.response.UserResponse;

public interface UserService {
    MessageResponse createUser(UserRequest userRequest);

    MessageResponse updateUser(Long id, UserRequest userRequest);

    PageResponse<UserResponse> getUsers(int page, int size, String search);

    MessageResponse deleteUser(Long id, String currentUserEmail);

    MessageResponse toggleUserStatus(Long id, String currentUserEmail);

    MessageResponse changeRole(Long id, UserRequest userRequest);
}
