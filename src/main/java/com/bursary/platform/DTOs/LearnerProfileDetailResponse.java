package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed learner profile (for providers)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerProfileDetailResponse {

    private Long learnerId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String schoolName;
    private String location;
    private BigDecimal householdIncome;
    private LocalDateTime joinedAt;

    // Academic history
    private List<AcademicYearResponse> academicHistory;

    // Follow status
    private Boolean isFollowing;
    private LocalDateTime followedAt;
}