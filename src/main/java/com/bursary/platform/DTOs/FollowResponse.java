package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowResponse {

    private Long followId;
    private Long providerId;
    private Long learnerId;
    private String learnerName;
    private String notes;
    private LocalDateTime followedAt;
}