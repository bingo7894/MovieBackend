package com.nerflix.clone.dao;

import com.nerflix.clone.entity.User;
import com.nerflix.clone.entity.Video;
import com.nerflix.clone.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByPasswordResetToken(String passwordResetToken);

    long countByRoleAndActive(Role role, boolean active);

    @Query(
            "SELECT u FROM User u WHERE "
                    + "LOWER(u.fullName) LIKE LOWER(CONCAT('%',:search,'%')) OR "
                    + "LOWER(u.email) LIKE LOWER(CONCAT('%',:search,'%'))")
    Page<User> searchUsers(@Param("search")String trim, Pageable pageable);

    long countByRole(Role role);

    @Query("SELECT v.id FROM User u JOIN u.watchList v WHERE u.email = :email AND v.id IN :videoIds")
    Set<Long> findWatchVideoIds(@Param("email") String email, List<Long> videoIds);


    @Query(
            "SELECT v FROM User u JOIN u.watchList v " +
                    "WHERE u.id = :userId AND v.published = true AND (" +
                    "LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(v.description) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Page<Video> searchWatchListByUserId(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);


    @Query("SELECT v FROM User u JOIN u.watchList v WHERE u.id = :userId AND v.published = true")
    Page<Video> findWatchVideoUserId(Long userId, Pageable pageable);
}
