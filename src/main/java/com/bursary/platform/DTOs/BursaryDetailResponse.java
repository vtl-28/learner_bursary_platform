package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for detailed bursary view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BursaryDetailResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private LocalDate applicationDeadline;
    private Boolean isActive;
    private String criteria; // JSON string of eligibility criteria
    private LocalDateTime createdAt;

    // Provider information
    private ProviderInfo provider;

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