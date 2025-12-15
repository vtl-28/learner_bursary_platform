package com.bursary.platform.Controllers;

import com.bursary.platform.DTOs.*;
import com.bursary.platform.Services.BursaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bursaries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bursary Management", description = "APIs for browsing and searching bursaries")
@SecurityRequirement(name = "Bearer Authentication")
public class BursaryController {

    private final BursaryService bursaryService;

    @GetMapping
    @Operation(summary = "Get all active bursaries", description = "Retrieve all active bursary programs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bursaries retrieved successfully")
    })
    public ResponseEntity<SuccessResponse<List<BursarySummaryResponse>>> getAllActiveBursaries() {
        log.info("Request received to get all active bursaries");

        List<BursarySummaryResponse> bursaries = bursaryService.getAllActiveBursaries();

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("Found %d active bursaries", bursaries.size()),
                        bursaries
                )
        );
    }

    @GetMapping("/available")
    @Operation(summary = "Get available bursaries", description = "Retrieve bursaries that are active and deadline has not passed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available bursaries retrieved successfully")
    })
    public ResponseEntity<SuccessResponse<List<BursarySummaryResponse>>> getAvailableBursaries() {
        log.info("Request received to get available bursaries");

        List<BursarySummaryResponse> bursaries = bursaryService.getAvailableBursaries();

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("Found %d available bursaries", bursaries.size()),
                        bursaries
                )
        );
    }

    @GetMapping("/search")
    @Operation(summary = "Search bursaries", description = "Search and filter bursaries based on various criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<SuccessResponse<List<BursarySummaryResponse>>> searchBursaries(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String providerType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) LocalDate deadlineAfter,
            @RequestParam(required = false) LocalDate deadlineBefore,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        log.info("Search request received with keyword: {}, providerType: {}, location: {}",
                keyword, providerType, location);

        BursarySearchRequest searchRequest = new BursarySearchRequest(
                keyword, minAmount, maxAmount, providerType, location,
                deadlineAfter, deadlineBefore, isActive, sortBy, sortDirection
        );

        List<BursarySummaryResponse> bursaries = bursaryService.searchBursaries(searchRequest);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("Found %d bursaries matching your criteria", bursaries.size()),
                        bursaries
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bursary details", description = "Retrieve detailed information about a specific bursary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bursary details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Bursary not found")
    })
    public ResponseEntity<SuccessResponse<BursaryDetailResponse>> getBursaryById(@PathVariable Long id) {
        log.info("Request received to get bursary details for ID: {}", id);

        BursaryDetailResponse bursary = bursaryService.getBursaryById(id);

        return ResponseEntity.ok(
                SuccessResponse.ok("Bursary details retrieved successfully", bursary)
        );
    }

    @GetMapping("/count")
    @Operation(summary = "Get active bursary count", description = "Get total number of active bursaries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<SuccessResponse<Long>> getActiveBursaryCount() {
        log.info("Request received to get active bursary count");

        long count = bursaryService.getActiveBursaryCount();

        return ResponseEntity.ok(
                SuccessResponse.ok("Active bursary count retrieved successfully", count)
        );
    }
}