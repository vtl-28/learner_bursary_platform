package com.bursary.platform.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a bursary provider organization
 */
@Entity
@Table(name = "providers", indexes = {
        @Index(name = "idx_providers_email", columnList = "email"),
        @Index(name = "idx_providers_org_type", columnList = "organization_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name must not exceed 255 characters")
    @Column(name = "organization_name", nullable = false, length = 255)
    private String organizationName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Size(max = 100, message = "Organization type must not exceed 100 characters")
    @Column(name = "organization_type", length = 100)
    private String organizationType; // Bank, NGO, Corporate, Government

    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Column(name = "location", length = 255)
    private String location;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship to Bursaries
    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Bursary> bursaries = new ArrayList<>();
}