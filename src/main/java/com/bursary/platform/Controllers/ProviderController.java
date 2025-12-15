package com.bursary.platform.Controllers;

import com.bursary.platform.DTOs.ProviderAuthResponse;
import com.bursary.platform.DTOs.ProviderLoginRequest;
import com.bursary.platform.DTOs.SuccessResponse;
import com.bursary.platform.Services.ProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Provider Management", description = "APIs for provider authentication")
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping("/login")
    @Operation(summary = "Provider login", description = "Authenticate provider with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<SuccessResponse<ProviderAuthResponse>> login(
            @Valid @RequestBody ProviderLoginRequest request) {
        log.info("Provider login request received for email: {}", request.getEmail());

        ProviderAuthResponse authResponse = providerService.login(request);

        return ResponseEntity.ok(SuccessResponse.ok("Login successful", authResponse));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Provider logout", description = "Logout the currently authenticated provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<Void>> logout() {
        Long providerId = getCurrentProviderId();
        log.info("Provider logout request received for provider ID: {}", providerId);

        providerService.logout(providerId);

        return ResponseEntity.ok(SuccessResponse.ok("Logout successful", null));
    }

    /**
     * Extract current provider ID from security context
     */
    private Long getCurrentProviderId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}