package com.bursary.platform.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a learner/student in the system
 * Maps to the 'learners' table in PostgreSQL
 */
@Entity
@Table(name = "learners", indexes = {
        @Index(name = "idx_learners_email", columnList = "email"),
        @Index(name = "idx_learners_household_income", columnList = "household_income"),
        @Index(name = "idx_learners_location", columnList = "location")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Learner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @JsonIgnore  // Never expose password in JSON responses
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Size(max = 255, message = "School name must not exceed 255 characters")
    @Column(name = "school_name", length = 255)
    private String schoolName;

    @Column(name = "household_income", precision = 12, scale = 2)
    private BigDecimal householdIncome;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Column(name = "location", length = 255)
    private String location;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Convenience method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Override toString to exclude sensitive data
    @Override
    public String toString() {
        return "Learner{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", schoolName='" + schoolName + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}