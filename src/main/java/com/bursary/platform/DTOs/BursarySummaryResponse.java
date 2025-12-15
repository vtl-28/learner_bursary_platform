package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for bursary list view (summary information)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BursarySummaryResponse {

    private Long id;
    private String title;
    private BigDecimal amount;
    private LocalDate applicationDeadline;
    private String providerName;
    private String providerType;
    private String providerLocation;
    private Boolean isActive;
    private Boolean isAvailable; // Combines isActive and deadline check
}