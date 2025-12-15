package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for provider to view applications with learner details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderApplicationResponse {

    private Long applicationId;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private BigDecimal awardAmount;
    private String notes;

    // Learner details
    private LearnerInfo learner;

    // Bursary details
    private BursaryInfo bursary;

    /**
     * Nested DTO for learner information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LearnerInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String schoolName;
        private BigDecimal householdIncome;
        private String location;
    }

    /**
     * Nested DTO for bursary information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BursaryInfo {
        private Long id;
        private String title;
        private BigDecimal amount;
        private LocalDate applicationDeadline;
    }
}