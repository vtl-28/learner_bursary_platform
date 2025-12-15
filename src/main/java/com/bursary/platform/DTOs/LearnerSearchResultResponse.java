package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for learner search results (summary view)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerSearchResultResponse {

    private Long learnerId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String schoolName;
    private String location;
    private BigDecimal householdIncome;

    // Academic summary
    private Integer currentGradeLevel;
    private Integer currentYear;
    private BigDecimal overallAverage; // Average across all terms
    private BigDecimal highestTermAverage;

    // Follow status
    private Boolean isFollowing;
}