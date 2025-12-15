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
public class LearnerAuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn; // in seconds
    private UserData user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserData {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private LocalDateTime createdAt;
    }
}