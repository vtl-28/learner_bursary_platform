package com.bursary.platform.Controllers;

import com.bursary.platform.DTOs.*;
import com.bursary.platform.Services.AcademicService;
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
@RequestMapping("/api/v1/academic")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Academic Results Management", description = "APIs for learners to manage their academic results")
@SecurityRequirement(name = "Bearer Authentication")
public class AcademicController {

    private final AcademicService academicService;

    @PostMapping("/years")
    @Operation(summary = "Create academic year", description = "Create a new academic year (e.g., Grade 10 in 2024)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Academic year created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "409", description = "Academic year already exists")
    })
    public ResponseEntity<SuccessResponse<AcademicYearResponse>> createAcademicYear(
            @Valid @RequestBody CreateAcademicYearRequest request) {
        Long learnerId = getCurrentLearnerId();
        log.info("Creating academic year for learner {}: year={}, grade={}",
                learnerId, request.getYear(), request.getGradeLevel());

        AcademicYearResponse response = academicService.createAcademicYear(learnerId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.created("Academic year created successfully", response));
    }

    @PostMapping("/years/{academicYearId}/terms")
    @Operation(summary = "Add term results", description = "Add term results with subject marks to an academic year")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Term results added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Academic year does not belong to you"),
            @ApiResponse(responseCode = "404", description = "Academic year not found"),
            @ApiResponse(responseCode = "409", description = "Term results already exist")
    })
    public ResponseEntity<SuccessResponse<TermResultResponse>> addTermResult(
            @PathVariable Long academicYearId,
            @Valid @RequestBody CreateTermResultRequest request) {
        Long learnerId = getCurrentLearnerId();
        log.info("Adding term {} results to academic year {} for learner {}",
                request.getTermNumber(), academicYearId, learnerId);

        TermResultResponse response = academicService.addTermResult(learnerId, academicYearId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.created("Term results added successfully", response));
    }

    @GetMapping("/my-results")
    @Operation(summary = "Get my academic results", description = "Retrieve all academic years with terms and subjects for the logged-in learner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<List<AcademicYearResponse>>> getMyAcademicYears() {
        Long learnerId = getCurrentLearnerId();
        log.info("Fetching academic results for learner {}", learnerId);

        List<AcademicYearResponse> results = academicService.getMyAcademicYears(learnerId);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("Found %d academic years", results.size()),
                        results
                )
        );
    }

    @GetMapping("/years/{academicYearId}")
    @Operation(summary = "Get specific academic year", description = "Retrieve details of a specific academic year with all terms")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Academic year retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Academic year does not belong to you"),
            @ApiResponse(responseCode = "404", description = "Academic year not found")
    })
    public ResponseEntity<SuccessResponse<AcademicYearResponse>> getAcademicYearById(
            @PathVariable Long academicYearId) {
        Long learnerId = getCurrentLearnerId();
        log.info("Fetching academic year {} for learner {}", academicYearId, learnerId);

        AcademicYearResponse response = academicService.getAcademicYearById(learnerId, academicYearId);

        return ResponseEntity.ok(SuccessResponse.ok("Academic year retrieved successfully", response));
    }

    @PutMapping("/terms/{termResultId}")
    @Operation(summary = "Update term results", description = "Update term results by replacing all subject marks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Term results updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Term does not belong to you"),
            @ApiResponse(responseCode = "404", description = "Term result not found")
    })
    public ResponseEntity<SuccessResponse<TermResultResponse>> updateTermResult(
            @PathVariable Long termResultId,
            @Valid @RequestBody CreateTermResultRequest request) {
        Long learnerId = getCurrentLearnerId();
        log.info("Updating term result {} for learner {}", termResultId, learnerId);

        TermResultResponse response = academicService.updateTermResult(learnerId, termResultId, request);

        return ResponseEntity.ok(SuccessResponse.ok("Term results updated successfully", response));
    }

    @DeleteMapping("/years/{academicYearId}")
    @Operation(summary = "Delete academic year", description = "Delete an academic year and all its terms/subjects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Academic year deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Academic year does not belong to you"),
            @ApiResponse(responseCode = "404", description = "Academic year not found")
    })
    public ResponseEntity<SuccessResponse<Void>> deleteAcademicYear(@PathVariable Long academicYearId) {
        Long learnerId = getCurrentLearnerId();
        log.info("Deleting academic year {} for learner {}", academicYearId, learnerId);

        academicService.deleteAcademicYear(learnerId, academicYearId);

        return ResponseEntity.ok(SuccessResponse.ok("Academic year deleted successfully", null));
    }

    /**
     * Extract current learner ID from security context
     */
    private Long getCurrentLearnerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}