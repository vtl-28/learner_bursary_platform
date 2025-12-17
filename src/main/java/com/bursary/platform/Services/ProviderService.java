package com.bursary.platform.Services;

import com.bursary.platform.DTOs.ProviderAuthResponse;
import com.bursary.platform.DTOs.ProviderLoginRequest;
import com.bursary.platform.Entities.Provider;
import com.bursary.platform.Exceptions.InvalidCredentialsException;
import com.bursary.platform.Repositories.ProviderRepository;
import com.bursary.platform.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final JwtTokenProvider jwtTokenProvider;


    /**
     * Provider login (plain text password comparison for hackathon)
     */
    @Transactional(readOnly = true)
    public ProviderAuthResponse login(ProviderLoginRequest request) {
        log.info("Login attempt for provider email: {}", request.getEmail());

        // Find provider by email
        Provider provider = providerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Simple password comparison (plain text - hackathon only!)
        if (!request.getPassword().equals(provider.getPasswordHash())) {
            log.warn("Invalid password attempt for provider: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("Provider logged in successfully: {}", provider.getOrganizationName());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(provider.getId(), provider.getEmail());
        long expiresIn = jwtTokenProvider.getExpirationTime();

        return ProviderAuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .provider(mapToProviderData(provider))
                .build();
    }

    /**
     * Provider logout (client-side token removal)
     */
    public void logout(Long providerId) {
        log.info("Provider logged out: {}", providerId);
        // With JWT, logout is handled client-side by removing the token
        // Server just logs the action
    }

    /**
     * Map Provider entity to ProviderData DTO
     */
    private ProviderAuthResponse.ProviderData mapToProviderData(Provider provider) {
        return ProviderAuthResponse.ProviderData.builder()
                .id(provider.getId())
                .organizationName(provider.getOrganizationName())
                .email(provider.getEmail())
                .organizationType(provider.getOrganizationType())
                .location(provider.getLocation())
                .createdAt(provider.getCreatedAt())
                .build();
    }
}