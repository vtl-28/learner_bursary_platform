package com.bursary.platform.Controllers;

import com.bursary.platform.DTOs.LearnerProfileDetailResponse;
import com.bursary.platform.DTOs.LearnerSearchRequest;
import com.bursary.platform.DTOs.LearnerSearchResultResponse;
import com.bursary.platform.DTOs.SuccessResponse;
import com.bursary.platform.Services.ProviderSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/providers/learners")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Provider Learner Search", description = "APIs for providers to search and view learner profiles")
@SecurityRequirement(name = "Bearer Authentication")
public class ProviderSearchController {

    private final ProviderSearchService providerSearchService;

    @GetMapping("/search")
    @Operation(summary = "Search learners", description = "Search for learners based on academic performance and other criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Provider JWT required")
    })
    public ResponseEntity<SuccessResponse<List<LearnerSearchResultResponse>>> searchLearners(
            @RequestParam(required = false) BigDecimal minAverageMark,
            @RequestParam(required = false) Integer gradeLevel,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal maxHouseholdIncome,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) BigDecimal minSubjectMark,
            @RequestParam(required = false) Integer year) {

        Long providerId = getCurrentProviderId();
        log.info("Provider {} searching learners", providerId);

        LearnerSearchRequest searchRequest = new LearnerSearchRequest(
                minAverageMark, gradeLevel, location, maxHouseholdIncome,
                subjectName, minSubjectMark, year
        );

        List<LearnerSearchResultResponse> results = providerSearchService.searchLearners(providerId, searchRequest);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("Found %d learners matching your criteria", results.size()),
                        results
                )
        );
    }

    @GetMapping("/{learnerId}/profile")
    @Operation(summary = "Get learner profile", description = "View detailed learner profile with full academic history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Learner not found")
    })
    public ResponseEntity<SuccessResponse<LearnerProfileDetailResponse>> getLearnerProfile(
            @PathVariable Long learnerId) {

        Long providerId = getCurrentProviderId();
        log.info("Provider {} viewing profile of learner {}", providerId, learnerId);

        LearnerProfileDetailResponse profile = providerSearchService.getLearnerProfile(providerId, learnerId);

        return ResponseEntity.ok(SuccessResponse.ok("Learner profile retrieved successfully", profile));
    }

    /**
     * Extract current provider ID from security context
     */
    private Long getCurrentProviderId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}