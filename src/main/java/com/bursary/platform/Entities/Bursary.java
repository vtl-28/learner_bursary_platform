package com.bursary.platform.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a bursary/scholarship program
 */
@Entity
@Table(name = "bursaries", indexes = {
        @Index(name = "idx_bursaries_provider", columnList = "provider_id"),
        @Index(name = "idx_bursaries_active", columnList = "is_active"),
        @Index(name = "idx_bursaries_deadline", columnList = "application_deadline")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bursary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @DecimalMin(value = "0.0", message = "Amount must be positive")
    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "criteria", columnDefinition = "TEXT")
    private String criteria; // Store JSONB as String, parse when needed

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship to Provider
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", insertable = false, updatable = false)
    @JsonIgnore
    private Provider provider;

    // Helper method to check if deadline has passed
    public boolean isDeadlinePassed() {
        return applicationDeadline != null && applicationDeadline.isBefore(LocalDate.now());
    }

    // Helper method to check if bursary is still available
    public boolean isAvailable() {
        return isActive && !isDeadlinePassed();
    }
}