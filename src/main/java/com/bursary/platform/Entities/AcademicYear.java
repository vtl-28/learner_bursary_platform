package com.bursary.platform.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an academic year for a learner
 */
@Entity
@Table(name = "academic_years",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_learner_year_grade",
                        columnNames = {"learner_id", "year", "grade_level"})
        },
        indexes = {
                @Index(name = "idx_academic_years_learner", columnList = "learner_id"),
                @Index(name = "idx_academic_years_year", columnList = "year"),
                @Index(name = "idx_academic_years_grade", columnList = "grade_level")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Column(name = "year", nullable = false)
    private Integer year; // e.g., 2024

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel; // e.g., 10, 11, 12

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", insertable = false, updatable = false)
    @JsonIgnore
    private Learner learner;

    @OneToMany(mappedBy = "academicYear", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TermResult> termResults = new ArrayList<>();
}