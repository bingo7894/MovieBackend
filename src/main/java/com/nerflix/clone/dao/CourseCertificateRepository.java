package com.nerflix.clone.dao;

import com.nerflix.clone.entity.CourseCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseCertificateRepository extends JpaRepository<CourseCertificate, Long> {

    Optional<CourseCertificate> findByUserIdAndCourseId(Long userId, Long courseId);

    Optional<CourseCertificate> findByCertificateCode(String certificateCode);

    List<CourseCertificate> findByUserIdOrderByIssuedAtDesc(Long userId);

    List<CourseCertificate> findByCourseId(Long courseId);

    long countByCourseId(Long courseId);

    void deleteByCourseId(Long courseId);

    void deleteByUserId(Long userId);
}
