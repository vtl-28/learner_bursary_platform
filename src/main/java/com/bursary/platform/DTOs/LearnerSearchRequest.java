package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for provider to search learners by academic criteria
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearnerSearchRequest {

    private BigDecimal minAverageMark; // e.g., 70.0
    private Integer gradeLevel; // 8, 9, 10, 11, 12
    private String location; // e.g., "Gauteng", "Cape Town"
    private BigDecimal maxHouseholdIncome; // For need-based bursaries
    private String subjectName; // e.g., "Mathematics"
    private BigDecimal minSubjectMark; // e.g., 75.0
    private Integer year; // e.g., 2024
}