package com.bursary.platform.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a learner's bursary application
 */
@Entity
@Table(name = "applications",
        indexes = {
                @Index(name = "idx_applications_learner", columnList = "learner_id"),
                @Index(name = "idx_applications_bursary", columnList = "bursary_id"),
                @Index(name = "idx_applications_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_learner_bursary", columnNames = {"learner_id", "bursary_id"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Column(name = "bursary_id", nullable = false)
    private Long bursaryId;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "submitted"; // draft, submitted, under_review, shortlisted, interview_scheduled, accepted, rejected

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "award_amount", precision = 12, scale = 2)
    private BigDecimal awardAmount;

    @Column(name = "documents", columnDefinition = "TEXT")
    @Lob
    private String documents; // JSON string for document URLs

    @Column(name = "notes", columnDefinition = "TEXT")
    @Lob
    private String notes; // Provider's internal notes

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships (LAZY loaded)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", insertable = false, updatable = false)
    private Learner learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bursary_id", insertable = false, updatable = false)
    private Bursary bursary;
}