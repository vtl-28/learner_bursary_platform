package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for application response with full bursary details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {

    private Long id;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;

    // Full bursary details embedded
    private BursaryInfo bursary;

    /**
     * Nested DTO for bursary information in application
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BursaryInfo {
        private Long id;
        private String title;
        private String description;
        private BigDecimal amount;
        private LocalDate applicationDeadline;
        private Boolean isActive;
        private ProviderInfo provider;
    }

    /**
     * Nested DTO for provider information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProviderInfo {
        private Long id;
        private String organizationName;
        private String organizationType;
        private String location;
    }
}