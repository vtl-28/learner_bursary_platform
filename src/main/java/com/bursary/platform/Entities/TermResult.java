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
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing term results within an academic year
 */
@Entity
@Table(name = "term_results",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_academic_year_term",
                        columnNames = {"academic_year_id", "term_number"})
        },
        indexes = {
                @Index(name = "idx_term_results_academic_year", columnList = "academic_year_id"),
                @Index(name = "idx_term_results_average", columnList = "average_mark")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @Column(name = "term_number", nullable = false)
    private Integer termNumber; // 1, 2, 3, 4

    @Column(name = "average_mark", precision = 5, scale = 2)
    private BigDecimal averageMark; // Calculated from subject marks

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", insertable = false, updatable = false)
    @JsonIgnore
    private AcademicYear academicYear;

    @OneToMany(mappedBy = "termResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SubjectMark> subjectMarks = new ArrayList<>();
}