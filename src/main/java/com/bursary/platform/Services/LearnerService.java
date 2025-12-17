package com.bursary.platform.Services;

import com.bursary.platform.DTOs.*;
import com.bursary.platform.Exceptions.*;
import com.bursary.platform.Entities.Learner;
import com.bursary.platform.Repositories.LearnerRepository;
import com.bursary.platform.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearnerService {

    private final LearnerRepository learnerRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Register a new learner
     */

    public LearnerAuthResponse signup(SignupRequest request) {
        log.info("Attempting to register new learner with email: {}", request.getEmail());

        // Validate email format
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check if email already exists
        if (learnerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }

        // Create new learner
        Learner learner = Learner.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .schoolName(request.getSchoolName())
                .householdIncome(request.getHouseholdIncome())
                .location(request.getLocation())
                .build();

        learner = learnerRepository.save(learner);
        learnerRepository.flush();
        log.info("Successfully registered learner with ID: {}", learner.getId());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(learner.getId(), learner.getEmail());
        long expiresIn = jwtTokenProvider.getExpirationTime();

        return LearnerAuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(mapToUserData(learner))
                .build();
    }

    /**
     * Authenticate a learner
     */
    @Transactional(readOnly = true)
    public LearnerAuthResponse login(LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        Learner learner = learnerRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));


        if (!passwordEncoder.matches(request.getPassword(), learner.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("Successful login for learner ID: {}", learner.getId());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(learner.getId(), learner.getEmail());
        long expiresIn = jwtTokenProvider.getExpirationTime();

        return LearnerAuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(mapToUserData(learner))
                .build();
    }

    /**
     * Logout a learner (invalidate token on client side)
     */
    public void logout(Long learnerId) {
        log.info("Logout for learner ID: {}", learnerId);
        // Token invalidation is handled client-side
        // Additional logic can be added here if needed (e.g., token blacklisting)
    }

    /**
     * Get learner profile with caching
     */

    @Transactional(readOnly = true)
    public LearnerProfileResponse getProfile(Long learnerId) {
        log.info("Fetching profile for learner ID: {}", learnerId);

        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with ID: " + learnerId));

        return mapToLearnerProfileResponse(learner);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean checkEmailExists(String email) {
        return learnerRepository.existsByEmail(email.toLowerCase());
    }

    /**
     * Update learner profile
     */
    @Transactional
    public LearnerProfileResponse updateProfile(Long learnerId, UpdateProfileRequest updateRequest) {
        log.info("Updating profile for learner ID: {}", learnerId);

        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with ID: " + learnerId));

        // Check if new email is different and already exists
        if (updateRequest.getEmail() != null
                && !learner.getEmail().equalsIgnoreCase(updateRequest.getEmail())
                && learnerRepository.existsByEmail(updateRequest.getEmail())) {
            throw new DuplicateEmailException("Email already in use: " + updateRequest.getEmail());
        }

        // Only update fields that are provided (not null)
        if (updateRequest.getFirstName() != null) {
            learner.setFirstName(updateRequest.getFirstName());
        }

        if (updateRequest.getLastName() != null) {
            learner.setLastName(updateRequest.getLastName());
        }

        if (updateRequest.getEmail() != null) {
            learner.setEmail(updateRequest.getEmail().toLowerCase());
        }

        if (updateRequest.getSchoolName() != null) {
            learner.setSchoolName(updateRequest.getSchoolName());
        }

        if (updateRequest.getHouseholdIncome() != null) {
            learner.setHouseholdIncome(updateRequest.getHouseholdIncome());
        }

        if (updateRequest.getLocation() != null) {
            learner.setLocation(updateRequest.getLocation());
        }

        learner = learnerRepository.save(learner);
        log.info("Successfully updated profile for learner ID: {}", learnerId);

        return mapToLearnerProfileResponse(learner);
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Map Learner to UserData
     */
    private LearnerAuthResponse.UserData mapToUserData(Learner learner) {
        return LearnerAuthResponse.UserData.builder()
                .id(learner.getId())
                .firstName(learner.getFirstName())
                .lastName(learner.getLastName())
                .email(learner.getEmail())
                .createdAt(learner.getCreatedAt())
                .build();
    }

    /**
     * Map Learner to ProfileResponse
     */
    private LearnerProfileResponse mapToLearnerProfileResponse(Learner learner) {
        return LearnerProfileResponse.builder()
                .id(learner.getId())
                .firstName(learner.getFirstName())
                .lastName(learner.getLastName())
                .email(learner.getEmail())
                .schoolName(learner.getSchoolName())
                .householdIncome(learner.getHouseholdIncome())
                .location(learner.getLocation())
                .build();
    }
}
