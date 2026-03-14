package com.nerflix.clone.dao;

import com.nerflix.clone.entity.User;
import com.nerflix.clone.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
