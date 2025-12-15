package com.bursary.platform.Controllers;

import com.bursary.platform.DTOs.NotificationResponse;
import com.bursary.platform.DTOs.SuccessResponse;
import com.bursary.platform.Services.NotificationService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "APIs for managing notifications")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all notifications", description = "Get all notifications for the logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<List<NotificationResponse>>> getMyNotifications() {
        UserContext context = getCurrentUserContext();
        log.info("Fetching notifications for {} {}", context.userType, context.userId);

        List<NotificationResponse> notifications = notificationService.getMyNotifications(
                context.userId,
                context.userType
        );

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("Found %d notifications", notifications.size()),
                        notifications
                )
        );
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Get all unread notifications for the logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<List<NotificationResponse>>> getUnreadNotifications() {
        UserContext context = getCurrentUserContext();
        log.info("Fetching unread notifications for {} {}", context.userType, context.userId);

        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(
                context.userId,
                context.userType
        );

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        String.format("You have %d unread notifications", notifications.size()),
                        notifications
                )
        );
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SuccessResponse<Long>> getUnreadCount() {
        UserContext context = getCurrentUserContext();
        log.info("Getting unread count for {} {}", context.userType, context.userId);

        long count = notificationService.getUnreadCount(context.userId, context.userType);

        return ResponseEntity.ok(SuccessResponse.ok("Unread count retrieved successfully", count));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark as read", description = "Mark a notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Notification does not belong to you"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<SuccessResponse<Void>> markAsRead(@PathVariable Long notificationId) {
        UserContext context = getCurrentUserContext();
        log.info("{} {} marking notification {} as read", context.userType, context.userId, notificationId);

        notificationService.markAsRead(context.userId, context.userType, notificationId);

        return ResponseEntity.ok(SuccessResponse.ok("Notification marked as read", null));
    }

    /**
     * Helper to determine if current user is learner or provider
     */
    private UserContext getCurrentUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();

        // Check the request path to determine user type
        // This is a simple approach - you could also store user type in JWT
        String requestPath = ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                .getRequest().getRequestURI();

        String userType = requestPath.contains("/providers/") ? "provider" : "learner";

        return new UserContext(userId, userType);
    }

    private static class UserContext {
        Long userId;
        String userType;

        UserContext(Long userId, String userType) {
            this.userId = userId;
            this.userType = userType;
        }
    }
}