package com.bursary.platform.Controllers;

import com.bursary.platform.DTOs.FollowLearnerRequest;
import com.bursary.platform.DTOs.FollowResponse;
import com.bursary.platform.DTOs.FollowedLearnerResponse;
import com.bursary.platform.DTOs.SuccessResponse;
import com.bursary.platform.Services.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers/follow")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Provider Follow Management", description = "APIs for providers to follow learners")
@SecurityRequirement(name = "Bearer Authentication")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{learnerId}")
    @Operation(summary = "Follow a learner", description = "Provider starts following a learner to track their academic progress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully following learner"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Provider JWT required"),
            @ApiResponse(responseCode = "404", description = "Learner not found"),
            @ApiResponse(responseCode = "409", description = "Already following this learner")
    })
    public ResponseEntity<SuccessResponse<FollowResponse>> followLearner(
            @PathVariable Long learnerId,
            @RequestBody(required = false) FollowLearnerRequest request) {

        Long providerId = getCurrentProviderId();
        log.info("Provider {} following learner {}", providerId, learnerId);

        FollowResponse response = followService.followLearner(providerId, learnerId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.created("Successfully following learner", response));
    }

    @DeleteMapping("/{learnerId}")
    @Operation(summary = "Unfollow a learner", description = "Provider stops following a learner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully unfollowed learner"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Follow relationship not found")
    })
    public ResponseEntity<SuccessResponse<Void>> unfollowLearner(@PathVariable Long learnerId) {
        Long providerId = getCurrentProviderId();
        log.info("Provider {} unfollowing learner {}", providerId, learnerId);

        followService.unfollowLearner(providerId, learnerId);

        return ResponseEntity.ok(SuccessResponse.ok("Successfully unfollowed learner", null));
    }

    @GetMapping("/following")
    @Operation(summary = "Get followed learners", description = "Get all learners that the provider is following")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Followed learners retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<List<FollowedLearnerResponse>>> getFollowedLearners() {
        Long providerId = getCurrentProviderId();
        log.info("Fetching followed learners for provider {}", providerId);

        List<FollowedLearnerResponse> follows = followService.getFollowedLearners(providerId);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("You are following %d learners", follows.size()),
                        follows
                )
        );
    }

    @GetMapping("/check/{learnerId}")
    @Operation(summary = "Check if following", description = "Check if provider is following a specific learner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<Boolean>> isFollowing(@PathVariable Long learnerId) {
        Long providerId = getCurrentProviderId();
        log.info("Checking if provider {} is following learner {}", providerId, learnerId);

        boolean isFollowing = followService.isFollowing(providerId, learnerId);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        isFollowing ? "You are following this learner" : "You are not following this learner",
                        isFollowing
                )
        );
    }

    /**
     * Extract current provider ID from security context
     */
    private Long getCurrentProviderId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}