package com.bursary.platform.Services;

import com.bursary.platform.DTOs.*;
import com.bursary.platform.Entities.Application;
import com.bursary.platform.Entities.Bursary;
import com.bursary.platform.Entities.Learner;
import com.bursary.platform.Entities.Provider;
import com.bursary.platform.Exceptions.DuplicateApplicationException;
import com.bursary.platform.Exceptions.ResourceNotFoundException;
import com.bursary.platform.Repositories.ApplicationRepository;
import com.bursary.platform.Repositories.BursaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final BursaryRepository bursaryRepository;

    /**
     * Apply for a bursary
     */
    @Transactional
    public ApplicationResponse applyForBursary(Long learnerId, CreateApplicationRequest request) {
        log.info("Learner {} attempting to apply for bursary {}", learnerId, request.getBursaryId());

        // Check if bursary exists
        Bursary bursary = bursaryRepository.findById(request.getBursaryId())
                .orElseThrow(() -> new ResourceNotFoundException("Bursary not found with ID: " + request.getBursaryId()));

        // Check if bursary is active
        if (!bursary.getIsActive()) {
            throw new IllegalArgumentException("This bursary is no longer active");
        }

        // Check if deadline has passed
//        if (bursary.isDeadlinePassed()) {
//            throw new IllegalArgumentException("Application deadline has passed for this bursary");
//        }

        // Check if already applied
        if (applicationRepository.existsByLearnerIdAndBursaryId(learnerId, request.getBursaryId())) {
            throw new DuplicateApplicationException("You have already applied to this bursary");
        }

        // Create application
        Application application = Application.builder()
                .learnerId(learnerId)
                .bursaryId(request.getBursaryId())
                .status("submitted")
                .submittedAt(LocalDateTime.now())
                .build();

        application = applicationRepository.save(application);
        log.info("Application created successfully with ID: {}", application.getId());

        return mapToResponse(application, bursary);
    }

    /**
     * Get all applications for a learner
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(Long learnerId) {
        log.info("Fetching applications for learner ID: {}", learnerId);

        List<Application> applications = applicationRepository.findByLearnerIdOrderBySubmittedAtDesc(learnerId);

        return applications.stream()
                .map(app -> {
                    Bursary bursary = app.getBursary();
                    return mapToResponse(app, bursary);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get specific application by ID
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long learnerId, Long applicationId) {
        log.info("Fetching application {} for learner {}", applicationId, learnerId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        // Verify the application belongs to this learner
        if (!application.getLearnerId().equals(learnerId)) {
            throw new IllegalArgumentException("You don't have permission to view this application");
        }

        return mapToResponse(application, application.getBursary());
    }

    /**
     * Check if learner has already applied to a bursary
     */
    @Transactional(readOnly = true)
    public ApplicationCheckResponse checkIfApplied(Long learnerId, Long bursaryId) {
        log.info("Checking if learner {} has applied to bursary {}", learnerId, bursaryId);

        Application application = applicationRepository.findByLearnerIdAndBursaryId(learnerId, bursaryId)
                .orElse(null);

        if (application == null) {
            return ApplicationCheckResponse.builder()
                    .hasApplied(false)
                    .build();
        }

        return ApplicationCheckResponse.builder()
                .hasApplied(true)
                .applicationId(application.getId())
                .status(application.getStatus())
                .build();
    }

    /**
     * Withdraw application (delete)
     */
    @Transactional
    public void withdrawApplication(Long learnerId, Long applicationId) {
        log.info("Learner {} attempting to withdraw application {}", learnerId, applicationId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        // Verify ownership
        if (!application.getLearnerId().equals(learnerId)) {
            throw new IllegalArgumentException("You don't have permission to withdraw this application");
        }

        // Only allow withdrawal if status is 'submitted' or 'draft'
        if (!application.getStatus().equals("submitted") && !application.getStatus().equals("draft")) {
            throw new IllegalArgumentException("Cannot withdraw application with status: " + application.getStatus());
        }

        applicationRepository.delete(application);
        log.info("Application {} withdrawn successfully", applicationId);
    }


    // ========== PROVIDER METHODS ==========

    /**
     * Get all applications received by a provider
     */
    @Transactional(readOnly = true)
    public List<ProviderApplicationResponse> getProviderApplications(Long providerId, String status) {
        log.info("Fetching applications for provider ID: {}, status filter: {}", providerId, status);

        List<Application> applications;

        if (status != null && !status.isEmpty()) {
            applications = applicationRepository.findByProviderIdAndStatus(providerId, status);
        } else {
            applications = applicationRepository.findByProviderId(providerId);
        }

        return applications.stream()
                .map(this::mapToProviderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get applications for a specific bursary
     */
    @Transactional(readOnly = true)
    public List<ProviderApplicationResponse> getBursaryApplications(Long providerId, Long bursaryId) {
        log.info("Fetching applications for bursary ID: {}", bursaryId);

        // Verify bursary belongs to this provider
        Bursary bursary = bursaryRepository.findById(bursaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Bursary not found with ID: " + bursaryId));

        if (!bursary.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException("This bursary does not belong to you");
        }

        List<Application> applications = applicationRepository.findByBursaryIdOrderBySubmittedAtDesc(bursaryId);

        return applications.stream()
                .map(this::mapToProviderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update application status (provider only)
     */
    @Transactional
    public ProviderApplicationResponse updateApplicationStatus(Long providerId, Long applicationId,
                                                               UpdateApplicationStatusRequest request) {
        log.info("Provider {} updating application {} to status: {}", providerId, applicationId, request.getStatus());

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        // Verify application belongs to this provider's bursary
        Bursary bursary = application.getBursary();
        if (!bursary.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException("This application does not belong to your bursaries");
        }

        // Update status
        application.setStatus(request.getStatus());
        application.setReviewedAt(LocalDateTime.now());

        // Update award amount if accepted
        if ("accepted".equals(request.getStatus()) && request.getAwardAmount() != null) {
            application.setAwardAmount(request.getAwardAmount());
        }

        // Update notes
        if (request.getNotes() != null) {
            application.setNotes(request.getNotes());
        }

        application = applicationRepository.save(application);
        log.info("Application {} status updated to: {}", applicationId, request.getStatus());

        return mapToProviderResponse(application);
    }

    /**
     * Get application statistics for provider
     */
    @Transactional(readOnly = true)
    public ApplicationStatisticsResponse getProviderStatistics(Long providerId) {
        log.info("Fetching application statistics for provider ID: {}", providerId);

        long total = applicationRepository.countByProviderId(providerId);
        long submitted = applicationRepository.countByProviderIdAndStatus(providerId, "submitted");
        long underReview = applicationRepository.countByProviderIdAndStatus(providerId, "under_review");
        long shortlisted = applicationRepository.countByProviderIdAndStatus(providerId, "shortlisted");
        long interviewScheduled = applicationRepository.countByProviderIdAndStatus(providerId, "interview_scheduled");
        long accepted = applicationRepository.countByProviderIdAndStatus(providerId, "accepted");
        long rejected = applicationRepository.countByProviderIdAndStatus(providerId, "rejected");

        Map<String, Long> byStatus = Map.of(
                "submitted", submitted,
                "under_review", underReview,
                "shortlisted", shortlisted,
                "interview_scheduled", interviewScheduled,
                "accepted", accepted,
                "rejected", rejected
        );

        return ApplicationStatisticsResponse.builder()
                .totalApplications(total)
                .submittedApplications(submitted)
                .underReviewApplications(underReview)
                .shortlistedApplications(shortlisted)
                .interviewScheduledApplications(interviewScheduled)
                .acceptedApplications(accepted)
                .rejectedApplications(rejected)
                .applicationsByStatus(byStatus)
                .build();
    }

    /**
     * Map Application to ProviderApplicationResponse (includes learner details)
     */
    private ProviderApplicationResponse mapToProviderResponse(Application application) {
        Learner learner = application.getLearner();
        Bursary bursary = application.getBursary();

        return ProviderApplicationResponse.builder()
                .applicationId(application.getId())
                .status(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .reviewedAt(application.getReviewedAt())
                .awardAmount(application.getAwardAmount())
                .notes(application.getNotes())
                .learner(ProviderApplicationResponse.LearnerInfo.builder()
                        .id(learner.getId())
                        .firstName(learner.getFirstName())
                        .lastName(learner.getLastName())
                        .fullName(learner.getFirstName() + " " + learner.getLastName())
                        .email(learner.getEmail())
                        .schoolName(learner.getSchoolName())
                        .householdIncome(learner.getHouseholdIncome())
                        .location(learner.getLocation())
                        .build())
                .bursary(ProviderApplicationResponse.BursaryInfo.builder()
                        .id(bursary.getId())
                        .title(bursary.getTitle())
                        .amount(bursary.getAmount())
                        .applicationDeadline(bursary.getApplicationDeadline())
                        .build())
                .build();
    }

    // ========== Helper Methods ==========

    /**
     * Map Application and Bursary entities to ApplicationResponse with full details
     */
    private ApplicationResponse mapToResponse(Application application, Bursary bursary) {
        Provider provider = bursary.getProvider();

        // Build provider info
        ApplicationResponse.ProviderInfo providerInfo = null;
        if (provider != null) {
            providerInfo = ApplicationResponse.ProviderInfo.builder()
                    .id(provider.getId())
                    .organizationName(provider.getOrganizationName())
                    .organizationType(provider.getOrganizationType())
                    .location(provider.getLocation())
                    .build();
        }

        // Build bursary info with provider
        ApplicationResponse.BursaryInfo bursaryInfo = ApplicationResponse.BursaryInfo.builder()
                .id(bursary.getId())
                .title(bursary.getTitle())
                .description(bursary.getDescription())
                .amount(bursary.getAmount())
                .applicationDeadline(bursary.getApplicationDeadline())
                .isActive(bursary.getIsActive())
                .provider(providerInfo)
                .build();

        // Build final response
        return ApplicationResponse.builder()
                .id(application.getId())
                .status(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .createdAt(application.getCreatedAt())
                .bursary(bursaryInfo)
                .build();
    }
}