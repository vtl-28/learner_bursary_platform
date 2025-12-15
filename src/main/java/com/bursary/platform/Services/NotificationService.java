package com.bursary.platform.Services;

import com.bursary.platform.DTOs.NotificationResponse;
import com.bursary.platform.Entities.Notification;
import com.bursary.platform.Exceptions.ResourceNotFoundException;
import com.bursary.platform.Repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Create notification when provider follows learner
     */
    @Transactional
    public void createFollowerNotification(Long learnerId, String providerName, Long followId) {
        log.info("Creating follower notification for learner {}", learnerId);

        Notification notification = Notification.builder()
                .userId(learnerId)
                .userType("learner")
                .notificationType("new_follower")
                .title("New Follower!")
                .message(String.format("%s is now following you", providerName))
                .relatedEntityType("follow")
                .relatedEntityId(followId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Follower notification created for learner {}", learnerId);
    }

    /**
     * Create notification when learner updates results
     */
    @Transactional
    public void createResultUpdateNotification(Long providerId, String learnerName, Long academicYearId) {
        log.info("Creating result update notification for provider {}", providerId);

        Notification notification = Notification.builder()
                .userId(providerId)
                .userType("provider")
                .notificationType("result_update")
                .title("Learner Updated Results")
                .message(String.format("%s has updated their academic results", learnerName))
                .relatedEntityType("academic_year")
                .relatedEntityId(academicYearId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Result update notification created for provider {}", providerId);
    }

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId, String userType) {
        log.info("Fetching notifications for {} {}", userType, userId);

        List<Notification> notifications = notificationRepository
                .findByUserIdAndUserTypeOrderByCreatedAtDesc(userId, userType);

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId, String userType) {
        log.info("Fetching unread notifications for {} {}", userType, userId);

        List<Notification> notifications = notificationRepository
                .findByUserIdAndUserTypeAndIsReadOrderByCreatedAtDesc(userId, userType, false);

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long userId, String userType, Long notificationId) {
        log.info("{} {} marking notification {} as read", userType, userId, notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        // Verify ownership
        if (!notification.getUserId().equals(userId) || !notification.getUserType().equals(userType)) {
            throw new IllegalArgumentException("This notification does not belong to you");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Get unread count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId, String userType) {
        return notificationRepository.countByUserIdAndUserTypeAndIsRead(userId, userType, false);
    }

    // ========== Helper Methods ==========

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}