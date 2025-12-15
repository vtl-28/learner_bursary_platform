package com.bursary.platform.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a notification for learners or providers
 */
@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_user", columnList = "user_id, user_type"),
                @Index(name = "idx_notifications_unread", columnList = "user_id, user_type, is_read"),
                @Index(name = "idx_notifications_created", columnList = "created_at")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Can be learner_id or provider_id

    @Column(name = "user_type", nullable = false, length = 20)
    private String userType; // 'learner' or 'provider'

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType; // 'new_follower', 'result_update', 'new_offer', 'new_application'

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    @Lob
    private String message;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // 'follow', 'academic_year', 'offer', 'application'

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}