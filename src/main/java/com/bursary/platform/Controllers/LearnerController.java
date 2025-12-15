package com.bursary.platform.Controllers;


import com.bursary.platform.DTOs.*;
import com.bursary.platform.Services.LearnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.bursary.platform.Services.FollowService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learners")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Learner Management", description = "APIs for learner registration, authentication, and profile management")
public class LearnerController {

    private final LearnerService learnerService;
    private final FollowService followService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new learner", description = "Create a new learner account with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Learner registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<SuccessResponse<LearnerAuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for email: {}", request.getEmail());

        LearnerAuthResponse authResponse = learnerService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.created("Registration successful", authResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Login learner", description = "Authenticate learner with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<SuccessResponse<LearnerAuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        LearnerAuthResponse authResponse = learnerService.login(request);

        return ResponseEntity.ok(SuccessResponse.ok("Login successful", authResponse));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Logout learner", description = "Logout the currently authenticated learner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<Void>> logout() {
        Long learnerId = getCurrentLearnerId();
        log.info("Logout request received for learner ID: {}", learnerId);

        learnerService.logout(learnerId);

        return ResponseEntity.ok(SuccessResponse.ok("Logout successful", null));
    }

    @GetMapping("/profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get learner profile", description = "Retrieve the profile of the currently authenticated learner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Learner not found")
    })
    public ResponseEntity<SuccessResponse<LearnerProfileResponse>> getProfile() {
        Long learnerId = getCurrentLearnerId();
        log.info("Profile request received for learner ID: {}", learnerId);

        LearnerProfileResponse profileResponse = learnerService.getProfile(learnerId);

        return ResponseEntity.ok(SuccessResponse.ok("Profile retrieved successfully", profileResponse));
    }

    @GetMapping("/check-email")
    @Operation(summary = "Check email availability", description = "Check if an email address is already registered")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email check completed")
    })
    public ResponseEntity<SuccessResponse<Boolean>> checkEmail(@RequestParam String email) {
        log.info("Email check request for: {}", email);

        boolean exists = learnerService.checkEmailExists(email);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        exists ? "Email already exists" : "Email is available",
                        exists
                )
        );
    }

    @PatchMapping("/profile")  // Changed from @PutMapping to @PatchMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update learner profile", description = "Update specific fields of the learner profile (partial update)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Learner not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<SuccessResponse<LearnerProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest) {  // Changed from SignupRequest
        Long learnerId = getCurrentLearnerId();
        log.info("Profile update request received for learner ID: {}", learnerId);

        LearnerProfileResponse profileResponse = learnerService.updateProfile(learnerId, updateRequest);

        return ResponseEntity.ok(SuccessResponse.ok("Profile updated successfully", profileResponse));
    }

    @GetMapping("/followers")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get my followers", description = "Get all providers following the logged-in learner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Followers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<List<FollowResponse>>> getMyFollowers() {
        Long learnerId = getCurrentLearnerId();
        log.info("Fetching followers for learner {}", learnerId);

        List<FollowResponse> followers = followService.getFollowers(learnerId);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("You have %d followers", followers.size()),
                        followers
                )
        );
    }

    @GetMapping("/followers/count")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get follower count", description = "Get total number of providers following this learner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<Long>> getFollowerCount() {
        Long learnerId = getCurrentLearnerId();
        log.info("Getting follower count for learner {}", learnerId);

        long count = followService.getFollowerCount(learnerId);

        return ResponseEntity.ok(SuccessResponse.ok("Follower count retrieved successfully", count));
    }

    /**
     * Extract current learner ID from security context
     */
    private Long getCurrentLearnerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}

