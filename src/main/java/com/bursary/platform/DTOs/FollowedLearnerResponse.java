package com.bursary.platform.DTOs;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowedLearnerResponse {

    // Learner fields
    private Long id;
    private String firstName;
    private String lastName;
    private String schoolName;
    private String location;
    private BigDecimal householdIncome;

    // Follow metadata
    private Long followId;
    private LocalDateTime followedAt;
    private String notes;
}
