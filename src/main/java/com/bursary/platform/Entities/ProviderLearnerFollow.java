package com.bursary.platform.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a provider following a learner
 */
@Entity
@Table(name = "provider_learner_follows",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_provider_learner_follow",
                        columnNames = {"provider_id", "learner_id"})
        },
        indexes = {
                @Index(name = "idx_provider_learner_follows_provider", columnList = "provider_id"),
                @Index(name = "idx_provider_learner_follows_learner", columnList = "learner_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderLearnerFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Column(name = "followed_at")
    private LocalDateTime followedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    @Lob
    private String notes; // Provider's notes about why they're following

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationships (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", insertable = false, updatable = false)
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", insertable = false, updatable = false)
    private Learner learner;
}