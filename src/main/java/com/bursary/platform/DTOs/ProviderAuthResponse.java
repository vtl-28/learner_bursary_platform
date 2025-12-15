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
public class ProviderAuthResponse {

    private String token;
    private String tokenType;
    private long expiresIn; // milliseconds
    private ProviderData provider;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProviderData {
        private Long id;
        private String organizationName;
        private String email;
        private String organizationType;
        private String location;
        private LocalDateTime createdAt;
    }
}