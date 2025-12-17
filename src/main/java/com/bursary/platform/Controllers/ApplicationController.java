package com.bursary.platform.Controllers;

import com.bursary.platform.DTOs.*;
import com.bursary.platform.Entities.Learner;
import com.bursary.platform.Exceptions.ResourceNotFoundException;
import com.bursary.platform.Exceptions.UnauthorizedException;
import com.bursary.platform.Repositories.LearnerRepository;
import com.bursary.platform.Services.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Application Management", description = "APIs for learners to apply for bursaries and manage applications")
@SecurityRequirement(name = "Bearer Authentication")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final LearnerRepository learnerRepository;

    @PostMapping
    @Operation(summary = "Apply for a bursary", description = "Submit an application for a specific bursary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or bursary inactive/deadline passed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "404", description = "Bursary not found"),
            @ApiResponse(responseCode = "409", description = "Already applied to this bursary")
    })
    public ResponseEntity<SuccessResponse<ApplicationResponse>> applyForBursary(
            @Valid @RequestBody CreateApplicationRequest request) {
        Long learnerId = getCurrentLearnerId();
        log.info("Application request received from learner {} for bursary {}", learnerId, request.getBursaryId());

        ApplicationResponse response = applicationService.applyForBursary(learnerId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.created("Application submitted successfully", response));
    }

    @GetMapping("/my-applications")
    @Operation(summary = "Get my applications", description = "Retrieve all applications submitted by the logged-in learner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<SuccessResponse<List<ApplicationResponse>>> getMyApplications() {
        Long learnerId = getCurrentLearnerId();
        log.info("Fetching applications for learner {}", learnerId);

        List<ApplicationResponse> applications = applicationService.getMyApplications(learnerId);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("Found %d applications", applications.size()),
                        applications
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application details", description = "Retrieve details of a specific application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not your application"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<SuccessResponse<ApplicationResponse>> getApplicationById(@PathVariable Long id) {
        Long learnerId = getCurrentLearnerId();
        log.info("Fetching application {} for learner {}", id, learnerId);

        ApplicationResponse response = applicationService.getApplicationById(learnerId, id);

        return ResponseEntity.ok(SuccessResponse.ok("Application retrieved successfully", response));
    }

    @GetMapping("/check/{bursaryId}")
    @Operation(summary = "Check if already applied", description = "Check if the learner has already applied to a specific bursary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<SuccessResponse<ApplicationCheckResponse>> checkIfApplied(@PathVariable Long bursaryId) {
        Long learnerId = getCurrentLearnerId();
        log.info("Checking if learner {} has applied to bursary {}", learnerId, bursaryId);

        ApplicationCheckResponse response = applicationService.checkIfApplied(learnerId, bursaryId);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        response.isHasApplied() ? "You have already applied" : "You can apply",
                        response
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Withdraw application", description = "Withdraw/delete an application (only if status is submitted or draft)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application withdrawn successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot withdraw application in current status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not your application"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<SuccessResponse<Void>> withdrawApplication(@PathVariable Long id) {
        Long learnerId = getCurrentLearnerId();
        log.info("Learner {} withdrawing application {}", learnerId, id);

        applicationService.withdrawApplication(learnerId, id);

        return ResponseEntity.ok(SuccessResponse.ok("Application withdrawn successfully", null));
    }


    // ========== PROVIDER ENDPOINTS ==========

    @GetMapping("/provider/received")
    @Operation(summary = "Get all applications received", description = "Provider views all applications to their bursaries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<SuccessResponse<List<ProviderApplicationResponse>>> getProviderApplications(
            @RequestParam(required = false) String status) {
        Long providerId = getCurrentProviderId();
        log.info("Provider {} fetching applications, status filter: {}", providerId, status);

        List<ProviderApplicationResponse> applications = applicationService.getProviderApplications(providerId, status);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("Found %d applications", applications.size()),
                        applications
                )
        );
    }

    @GetMapping("/provider/bursary/{bursaryId}")
    @Operation(summary = "Get applications for specific bursary", description = "Provider views applications for a specific bursary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Bursary does not belong to you"),
            @ApiResponse(responseCode = "404", description = "Bursary not found")
    })
    public ResponseEntity<SuccessResponse<List<ProviderApplicationResponse>>> getBursaryApplications(
            @PathVariable Long bursaryId) {
        Long providerId = getCurrentProviderId();
        log.info("Provider {} fetching applications for bursary {}", providerId, bursaryId);

        List<ProviderApplicationResponse> applications = applicationService.getBursaryApplications(providerId, bursaryId);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("Found %d applications for this bursary", applications.size()),
                        applications
                )
        );
    }

    @PatchMapping("/provider/{applicationId}/status")
    @Operation(summary = "Update application status", description = "Provider updates the status of an application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Application does not belong to your bursaries"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<SuccessResponse<ProviderApplicationResponse>> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        Long providerId = getCurrentProviderId();
        log.info("Provider {} updating application {} status", providerId, applicationId);

        ProviderApplicationResponse response = applicationService.updateApplicationStatus(providerId, applicationId, request);

        return ResponseEntity.ok(SuccessResponse.ok("Application status updated successfully", response));
    }

    @GetMapping("/provider/statistics")
    @Operation(summary = "Get application statistics", description = "Provider views statistics about all their applications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<ApplicationStatisticsResponse>> getProviderStatistics() {
        Long providerId = getCurrentProviderId();
        log.info("Provider {} fetching application statistics", providerId);

        ApplicationStatisticsResponse statistics = applicationService.getProviderStatistics(providerId);

        return ResponseEntity.ok(SuccessResponse.ok("Statistics retrieved successfully", statistics));
    }

    /**
     * Extract current provider ID from security context
     */
    private Long getCurrentProviderId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }

    /**
     * Extract current learner ID from security context
     */
    private Long getCurrentLearnerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return (Long) authentication.getPrincipal();
    }
}