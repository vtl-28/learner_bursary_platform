package com.bursary.platform.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing individual subject marks for a term
 */
@Entity
@Table(name = "subject_marks",
        indexes = {
                @Index(name = "idx_subject_marks_term_result", columnList = "term_result_id"),
                @Index(name = "idx_subject_marks_subject", columnList = "subject_name"),
                @Index(name = "idx_subject_marks_mark", columnList = "mark"),
                @Index(name = "idx_subject_marks_subject_mark", columnList = "subject_name, mark")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "term_result_id", nullable = false)
    private Long termResultId;

    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName; // e.g., "Mathematics", "Physical Sciences"

    @Column(name = "mark", nullable = false, precision = 5, scale = 2)
    private BigDecimal mark; // e.g., 85.5

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_result_id", insertable = false, updatable = false)
    @JsonIgnore
    private TermResult termResult;
}