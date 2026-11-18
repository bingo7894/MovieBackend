package com.nerflix.clone.controller;

import com.nerflix.clone.dto.response.CertificateResponse;
import com.nerflix.clone.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Certificates", description = "Course completion certificates and public verification")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Operation(summary = "Claim or retrieve completion certificate for a course")
    @PostMapping("/courses/{courseId}/certificate")
    public ResponseEntity<CertificateResponse> issueOrGetCertificate(
            @PathVariable Long courseId,
            Authentication authentication) {
        return ResponseEntity.ok(certificateService.issueOrGetCertificate(courseId, authentication.getName()));
    }

    @Operation(summary = "Get all certificates earned by current learner")
    @GetMapping("/certificates/my")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates(Authentication authentication) {
        return ResponseEntity.ok(certificateService.getMyCertificates(authentication.getName()));
    }

    @Operation(summary = "Verify a certificate publicly by certificate code")
    @GetMapping("/certificates/verify/{code}")
    public ResponseEntity<CertificateResponse> verifyCertificate(@PathVariable String code) {
        return ResponseEntity.ok(certificateService.verifyCertificate(code));
    }
}
