package com.nerflix.clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    private Long id;
    private String certificateCode;
    private Long courseId;
    private String courseTitle;
    private String category;
    private String level;
    private String recipientName;
    private String recipientEmail;
    private int totalEpisodes;
    private long totalDuration;
    private Instant issuedAt;
    private boolean valid;
}
