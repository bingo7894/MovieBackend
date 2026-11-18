package com.nerflix.clone.serviceimpl;

import com.nerflix.clone.dao.CourseCertificateRepository;
import com.nerflix.clone.dao.LessonProgressRepository;
import com.nerflix.clone.dao.QuizAttemptRepository;
import com.nerflix.clone.dto.response.CertificateResponse;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.exception.ResourceNotFoundException;
import com.nerflix.clone.service.CertificateService;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertificateServiceImpl implements CertificateService {

    @Autowired
    private CourseCertificateRepository certificateRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private LmsMapper lmsMapper;

    @Override
    @Transactional
    public CertificateResponse issueOrGetCertificate(Long courseId, String email) {
        Course course = serviceUtils.getCourseByIdOrThrow(courseId);
        if (!course.isPublished()) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        User user = serviceUtils.getUserByEmailOrThrow(email);

        Optional<CourseCertificate> existing = certificateRepository.findByUserIdAndCourseId(user.getId(), courseId);
        if (existing.isPresent()) {
            return lmsMapper.toCertificateResponse(existing.get());
        }

        List<Episode> publishedEpisodes = course.getTopics().stream()
                .flatMap(t -> t.getEpisodes().stream())
                .filter(Episode::isPublished)
                .toList();

        if (publishedEpisodes.isEmpty()) {
            throw new IllegalArgumentException("Course has no published episodes yet.");
        }

        List<Long> episodeIds = publishedEpisodes.stream().map(Episode::getId).toList();
        Set<Long> completedEpisodeIds = lessonProgressRepository.findByUserIdAndEpisodeIdIn(user.getId(), episodeIds)
                .stream()
                .filter(LessonProgress::isCompleted)
                .map(lp -> lp.getEpisode().getId())
                .collect(Collectors.toSet());

        if (completedEpisodeIds.size() < publishedEpisodes.size()) {
            throw new IllegalArgumentException("Complete 100% of published episodes before claiming your certificate.");
        }

        List<Long> publishedQuizIds = publishedEpisodes.stream()
                .flatMap(ep -> ep.getQuizzes().stream())
                .filter(Quiz::isPublished)
                .map(Quiz::getId)
                .toList();

        if (!publishedQuizIds.isEmpty()) {
            Set<Long> passedQuizIds = quizAttemptRepository.findByUserIdAndQuizIdIn(user.getId(), publishedQuizIds)
                    .stream()
                    .filter(QuizAttempt::isPassed)
                    .map(a -> a.getQuiz().getId())
                    .collect(Collectors.toSet());
            if (passedQuizIds.size() < publishedQuizIds.size()) {
                throw new IllegalArgumentException("Pass all course exercises before claiming your certificate.");
            }
        }

        long totalDuration = publishedEpisodes.stream()
                .mapToLong(ep -> ep.getDuration() != null ? ep.getDuration() : 0L)
                .sum();

        CourseCertificate cert = new CourseCertificate();
        cert.setCertificateCode(generateUniqueCode());
        cert.setUser(user);
        cert.setCourse(course);
        cert.setRecipientName(user.getFullName());
        cert.setCourseTitle(course.getTitle());
        cert.setTotalEpisodes(publishedEpisodes.size());
        cert.setTotalDuration(totalDuration);

        return lmsMapper.toCertificateResponse(certificateRepository.save(cert));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponse> getMyCertificates(String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(user.getId()).stream()
                .map(lmsMapper::toCertificateResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponse verifyCertificate(String code) {
        if (code == null || code.isBlank()) {
            throw new ResourceNotFoundException("Certificate not found");
        }
        CourseCertificate cert = certificateRepository.findByCertificateCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with code: " + code));
        return lmsMapper.toCertificateResponse(cert);
    }

    private String generateUniqueCode() {
        String candidate;
        do {
            candidate = "PL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (certificateRepository.findByCertificateCode(candidate).isPresent());
        return candidate;
    }
}
