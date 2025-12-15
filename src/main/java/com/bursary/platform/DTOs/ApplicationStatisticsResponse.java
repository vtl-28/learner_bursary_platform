package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for application statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatisticsResponse {

    private long totalApplications;
    private long submittedApplications;
    private long underReviewApplications;
    private long shortlistedApplications;
    private long interviewScheduledApplications;
    private long acceptedApplications;
    private long rejectedApplications;
    private Map<String, Long> applicationsByStatus;
    private Map<Long, Long> applicationsByBursary; // bursaryId -> count
}