package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowLearnerRequest {

    private String notes; // Optional: why provider is following this learner
}