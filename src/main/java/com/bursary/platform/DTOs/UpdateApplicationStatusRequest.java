package com.bursary.platform.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for provider to update application status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "submitted|under_review|shortlisted|interview_scheduled|accepted|rejected",
            message = "Status must be one of: submitted, under_review, shortlisted, interview_scheduled, accepted, rejected"
    )
    private String status;

    private BigDecimal awardAmount; // Only for 'accepted' status

    private String notes; // Provider's internal notes
}