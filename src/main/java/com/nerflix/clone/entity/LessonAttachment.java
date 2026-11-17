package com.nerflix.clone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "lesson_attachments")
@Getter
@Setter
@NoArgsConstructor
public class LessonAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String fileUuid;

    private Long fileSize;

    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
