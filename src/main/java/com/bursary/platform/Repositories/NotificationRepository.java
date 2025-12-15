package com.bursary.platform.Repositories;

import com.bursary.platform.Entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Get all notifications for a user
     */
    List<Notification> findByUserIdAndUserTypeOrderByCreatedAtDesc(Long userId, String userType);

    /**
     * Get unread notifications for a user
     */
    List<Notification> findByUserIdAndUserTypeAndIsReadOrderByCreatedAtDesc(Long userId, String userType, Boolean isRead);

    /**
     * Count unread notifications
     */
    long countByUserIdAndUserTypeAndIsRead(Long userId, String userType, Boolean isRead);

    /**
     * Mark all as read for a user
     */
    void deleteByUserIdAndUserType(Long userId, String userType);
}