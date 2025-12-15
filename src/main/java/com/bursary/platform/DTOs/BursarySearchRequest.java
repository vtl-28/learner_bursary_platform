package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for bursary search/filter parameters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BursarySearchRequest {

    private String keyword; // Search in title and description
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String providerType; // Bank, NGO, Corporate, Government
    private String location;
    private LocalDate deadlineAfter; // Only show bursaries with deadline after this date
    private LocalDate deadlineBefore; // Only show bursaries with deadline before this date
    private Boolean isActive; // Default: true
    private String sortBy; // amount, deadline, createdAt
    private String sortDirection; // asc, desc
}