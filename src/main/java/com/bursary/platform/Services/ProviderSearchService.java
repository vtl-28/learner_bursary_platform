package com.bursary.platform.Services;

import com.bursary.platform.DTOs.AcademicYearResponse;
import com.bursary.platform.DTOs.LearnerProfileDetailResponse;
import com.bursary.platform.DTOs.LearnerSearchRequest;
import com.bursary.platform.DTOs.LearnerSearchResultResponse;
import com.bursary.platform.Entities.*;
import com.bursary.platform.Exceptions.ResourceNotFoundException;
import com.bursary.platform.Repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderSearchService {

    private final LearnerRepository learnerRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TermResultRepository termResultRepository;
    private final SubjectMarkRepository subjectMarkRepository;
    private final AcademicService academicService;
    private final FollowService followService;
    private final ProviderLearnerFollowRepository followRepository;

    /**
     * Search learners by academic criteria
     */
    @Transactional(readOnly = true)
    public List<LearnerSearchResultResponse> searchLearners(Long providerId, LearnerSearchRequest searchRequest) {
        log.info("Provider {} searching learners with criteria: {}", providerId, searchRequest);

        // Get all learners (we'll filter in memory for hackathon simplicity)
        List<Learner> allLearners = learnerRepository.findAll();

        // Filter learners based on criteria
        List<LearnerSearchResultResponse> results = allLearners.stream()
                .map(learner -> buildSearchResult(learner, searchRequest, providerId))
                .filter(Objects::nonNull) // Remove learners who don't match criteria
                .sorted(Comparator.comparing(LearnerSearchResultResponse::getOverallAverage).reversed())
                .collect(Collectors.toList());

        log.info("Found {} learners matching criteria", results.size());
        return results;
    }

    /**
     * Get detailed learner profile for provider
     */
    @Transactional(readOnly = true)
    public LearnerProfileDetailResponse getLearnerProfile(Long providerId, Long learnerId) {
        log.info("Provider {} fetching profile for learner {}", providerId, learnerId);

        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with ID: " + learnerId));

        // Get academic history
        List<AcademicYear> academicYears = academicYearRepository.findByLearnerIdOrderByYearDescGradeLevelDesc(learnerId);

        List<AcademicYearResponse> academicHistory = academicYears.stream()
                .map(this::mapToAcademicYearResponse)
                .collect(Collectors.toList());

        // Check if provider is following and get follow date
        boolean isFollowing = false;
        LocalDateTime followedAt = null;

        Optional<ProviderLearnerFollow> followOpt = followRepository.findByProviderIdAndLearnerId(providerId, learnerId);
        if (followOpt.isPresent()) {
            isFollowing = true;
            followedAt = followOpt.get().getFollowedAt();
        }

        return LearnerProfileDetailResponse.builder()
                .learnerId(learner.getId())
                .firstName(learner.getFirstName())
                .lastName(learner.getLastName())
                .fullName(learner.getFirstName() + " " + learner.getLastName())
                .email(learner.getEmail())
                .schoolName(learner.getSchoolName())
                .location(learner.getLocation())
                .householdIncome(learner.getHouseholdIncome())
                .joinedAt(learner.getCreatedAt())
                .academicHistory(academicHistory)
                .isFollowing(isFollowing)
                .followedAt(null)
                .build();
    }

    // ========== Helper Methods ==========

    /**
     * Build search result for a learner, return null if doesn't match criteria
     */
    private LearnerSearchResultResponse buildSearchResult(Learner learner,
                                                          LearnerSearchRequest criteria,
                                                          Long providerId) {
        try {
            // Get learner's academic years
            List<AcademicYear> academicYears = academicYearRepository.findByLearnerIdOrderByYearDescGradeLevelDesc(learner.getId());

            if (academicYears.isEmpty()) {
                return null; // Skip learners with no academic records
            }

            // Get most recent academic year
            AcademicYear latestYear = academicYears.get(0);

            // Filter by grade level
            if (criteria.getGradeLevel() != null && !latestYear.getGradeLevel().equals(criteria.getGradeLevel())) {
                return null;
            }

            // Filter by year
            if (criteria.getYear() != null && !latestYear.getYear().equals(criteria.getYear())) {
                return null;
            }

            // Filter by location
            if (criteria.getLocation() != null && learner.getLocation() != null) {
                if (!learner.getLocation().toLowerCase().contains(criteria.getLocation().toLowerCase())) {
                    return null;
                }
            }

            // Filter by household income
            if (criteria.getMaxHouseholdIncome() != null && learner.getHouseholdIncome() != null) {
                if (learner.getHouseholdIncome().compareTo(criteria.getMaxHouseholdIncome()) > 0) {
                    return null;
                }
            }

            // Get all term results for this academic year
            List<TermResult> termResults = termResultRepository.findByAcademicYearIdOrderByTermNumberAsc(latestYear.getId());

            if (termResults.isEmpty()) {
                return null;
            }

            // Calculate overall average
            BigDecimal overallAverage = calculateOverallAverage(termResults);
            BigDecimal highestTermAverage = termResults.stream()
                    .map(TermResult::getAverageMark)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            // Filter by minimum average
            if (criteria.getMinAverageMark() != null) {
                if (overallAverage.compareTo(criteria.getMinAverageMark()) < 0) {
                    return null;
                }
            }

            // Filter by subject performance
            if (criteria.getSubjectName() != null && criteria.getMinSubjectMark() != null) {
                boolean meetsSubjectCriteria = checkSubjectCriteria(
                        termResults,
                        criteria.getSubjectName(),
                        criteria.getMinSubjectMark()
                );

                if (!meetsSubjectCriteria) {
                    return null;
                }
            }

            // Check if provider is following
            boolean isFollowing = followService.isFollowing(providerId, learner.getId());

            // Build result
            return LearnerSearchResultResponse.builder()
                    .learnerId(learner.getId())
                    .firstName(learner.getFirstName())
                    .lastName(learner.getLastName())
                    .fullName(learner.getFirstName() + " " + learner.getLastName())
                    .schoolName(learner.getSchoolName())
                    .location(learner.getLocation())
                    .householdIncome(learner.getHouseholdIncome())
                    .currentGradeLevel(latestYear.getGradeLevel())
                    .currentYear(latestYear.getYear())
                    .overallAverage(overallAverage)
                    .highestTermAverage(highestTermAverage)
                    .isFollowing(isFollowing)
                    .build();

        } catch (Exception e) {
            log.warn("Error building search result for learner {}: {}", learner.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Calculate overall average across all terms
     */
    private BigDecimal calculateOverallAverage(List<TermResult> termResults) {
        if (termResults.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = termResults.stream()
                .map(TermResult::getAverageMark)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(termResults.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Check if learner meets subject criteria
     */
    private boolean checkSubjectCriteria(List<TermResult> termResults, String subjectName, BigDecimal minMark) {
        for (TermResult term : termResults) {
            List<SubjectMark> subjects = subjectMarkRepository.findByTermResultIdOrderBySubjectNameAsc(term.getId());

            for (SubjectMark subject : subjects) {
                if (subject.getSubjectName().equalsIgnoreCase(subjectName)) {
                    if (subject.getMark().compareTo(minMark) >= 0) {
                        return true; // Found at least one term where subject meets criteria
                    }
                }
            }
        }

        return false;
    }

    /**
     * Map AcademicYear to response with terms and subjects
     */
    private AcademicYearResponse mapToAcademicYearResponse(AcademicYear academicYear) {
        List<TermResult> termResults = termResultRepository.findByAcademicYearIdOrderByTermNumberAsc(academicYear.getId());

        // Use AcademicService helper or build manually
        // For simplicity, we'll build a basic response
        return AcademicYearResponse.builder()
                .id(academicYear.getId())
                .year(academicYear.getYear())
                .gradeLevel(academicYear.getGradeLevel())
                .createdAt(academicYear.getCreatedAt())
            .terms(List.of()) // Can populate fully if needed
                .build();
    }
}