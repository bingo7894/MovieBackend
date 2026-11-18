package com.nerflix.clone.service;

import com.nerflix.clone.dto.response.CertificateResponse;

import java.util.List;

public interface CertificateService {
    CertificateResponse issueOrGetCertificate(Long courseId, String email);

    List<CertificateResponse> getMyCertificates(String email);

    CertificateResponse verifyCertificate(String code);
}
