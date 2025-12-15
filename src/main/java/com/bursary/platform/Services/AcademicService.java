package com.bursary.platform.Services;

import com.bursary.platform.DTOs.*;
import com.bursary.platform.Entities.AcademicYear;
import com.bursary.platform.Entities.Learner;
import com.bursary.platform.Entities.SubjectMark;
import com.bursary.platform.Entities.TermResult;
import com.bursary.platform.Exceptions.DuplicateResourceException;
import com.bursary.platform.Exceptions.ResourceNotFoundException;
import com.bursary.platform.Repositories.AcademicYearRepository;
import com.bursary.platform.Repositories.LearnerRepository;
import com.bursary.platform.Repositories.SubjectMarkRepository;
import com.bursary.platform.Repositories.TermResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicService {

    private final AcademicYearRepository academicYearRepository;
    private final TermResultRepository termResultRepository;
    private final SubjectMarkRepository subjectMarkRepository;
    private final NotificationService notificationService;
    private final FollowService followService;
    private final LearnerRepository learnerRepository;

    /**
     * Create a new academic year for a learner
     */
    @Transactional
    public AcademicYearResponse createAcademicYear(Long learnerId, CreateAcademicYearRequest request) {
        log.info("Creating academic year {} grade {} for learner {}", request.getYear(), request.getGradeLevel(), learnerId);

        // Check if already exists
        if (academicYearRepository.existsByLearnerIdAndYearAndGradeLevel(
                learnerId, request.getYear(), request.getGradeLevel())) {
            throw new DuplicateResourceException(
                    String.format("Academic year %d grade %d already exists for this learner",
                            request.getYear(), request.getGradeLevel()));
        }

        // Create academic year
        AcademicYear academicYear = AcademicYear.builder()
                .learnerId(learnerId)
                .year(request.getYear())
                .gradeLevel(request.getGradeLevel())
                .build();

        academicYear = academicYearRepository.save(academicYear);
        log.info("Academic year created with ID: {}", academicYear.getId());

        return mapToAcademicYearResponse(academicYear);
    }

    /**
     * Add term results with subject marks
     */
    @Transactional
    public TermResultResponse addTermResult(Long learnerId, Long academicYearId, CreateTermResultRequest request) {
        log.info("Adding term {} results to academic year {} for learner {}",
                request.getTermNumber(), academicYearId, learnerId);

        // Verify academic year belongs to this learner
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + academicYearId));

        if (!academicYear.getLearnerId().equals(learnerId)) {
            throw new IllegalArgumentException("This academic year does not belong to you");
        }

        // Check if term already exists
        if (termResultRepository.existsByAcademicYearIdAndTermNumber(academicYearId, request.getTermNumber())) {
            throw new DuplicateResourceException(
                    String.format("Term %d results already exist for this academic year", request.getTermNumber()));
        }

        // Calculate average mark from subjects
        BigDecimal averageMark = calculateAverage(request.getSubjects());

        // Create term result
        TermResult termResult = TermResult.builder()
                .academicYearId(academicYearId)
                .termNumber(request.getTermNumber())
                .averageMark(averageMark)
                .build();

        termResult = termResultRepository.save(termResult);
        log.info("Term result created with ID: {} and average: {}", termResult.getId(), averageMark);

        // Create subject marks
        Long termResultId = termResult.getId();
        List<SubjectMark> subjectMarks = request.getSubjects().stream()
                .map(subject -> SubjectMark.builder()
                        .termResultId(termResultId)
                        .subjectName(subject.getSubjectName())
                        .mark(BigDecimal.valueOf(subject.getMark()))
                        .build())
                .collect(Collectors.toList());

        subjectMarks = subjectMarkRepository.saveAll(subjectMarks);
        log.info("Created {} subject marks for term {}", subjectMarks.size(), request.getTermNumber());

        // Notify followers about new results
        notifyFollowersOfUpdate(learnerId, academicYearId);

        return mapToTermResultResponse(termResult, subjectMarks);
    }

    /**
     * Get all academic years for a learner
     */
    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getMyAcademicYears(Long learnerId) {
        log.info("Fetching academic years for learner {}", learnerId);

        List<AcademicYear> academicYears = academicYearRepository.findByLearnerIdOrderByYearDescGradeLevelDesc(learnerId);

        return academicYears.stream()
                .map(this::mapToAcademicYearResponseWithTerms)
                .collect(Collectors.toList());
    }

    /**
     * Get specific academic year with all terms
     */
    @Transactional(readOnly = true)
    public AcademicYearResponse getAcademicYearById(Long learnerId, Long academicYearId) {
        log.info("Fetching academic year {} for learner {}", academicYearId, learnerId);

        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + academicYearId));

        if (!academicYear.getLearnerId().equals(learnerId)) {
            throw new IllegalArgumentException("This academic year does not belong to you");
        }

        return mapToAcademicYearResponseWithTerms(academicYear);
    }

    /**
     * Update term results (replace subject marks)
     */
    @Transactional
    public TermResultResponse updateTermResult(Long learnerId, Long termResultId, CreateTermResultRequest request) {
        log.info("Updating term result {} for learner {}", termResultId, learnerId);

        TermResult termResult = termResultRepository.findById(termResultId)
                .orElseThrow(() -> new ResourceNotFoundException("Term result not found with ID: " + termResultId));

        // Verify ownership through academic year
        AcademicYear academicYear = academicYearRepository.findById(termResult.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        if (!academicYear.getLearnerId().equals(learnerId)) {
            throw new IllegalArgumentException("You don't have permission to update this term result");
        }

        // Verify term number matches
        if (!termResult.getTermNumber().equals(request.getTermNumber())) {
            throw new IllegalArgumentException("Cannot change term number. Delete and recreate instead.");
        }

        // Delete old subject marks
        subjectMarkRepository.deleteByTermResultId(termResultId);

        // Calculate new average
        BigDecimal averageMark = calculateAverage(request.getSubjects());
        termResult.setAverageMark(averageMark);
        termResult = termResultRepository.save(termResult);

        // Create new subject marks
        List<SubjectMark> subjectMarks = request.getSubjects().stream()
                .map(subject -> SubjectMark.builder()
                        .termResultId(termResultId)
                        .subjectName(subject.getSubjectName())
                        .mark(BigDecimal.valueOf(subject.getMark()))
                        .build())
                .collect(Collectors.toList());

        subjectMarks = subjectMarkRepository.saveAll(subjectMarks);
        log.info("Updated term result with {} subjects, new average: {}", subjectMarks.size(), averageMark);

        // Notify followers about updated results
        notifyFollowersOfUpdate(learnerId, termResult.getAcademicYearId());

        return mapToTermResultResponse(termResult, subjectMarks);
    }

    /**
     * Delete academic year (cascades to terms and subjects)
     */
    @Transactional
    public void deleteAcademicYear(Long learnerId, Long academicYearId) {
        log.info("Deleting academic year {} for learner {}", academicYearId, learnerId);

        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + academicYearId));

        if (!academicYear.getLearnerId().equals(learnerId)) {
            throw new IllegalArgumentException("You don't have permission to delete this academic year");
        }

        academicYearRepository.delete(academicYear);
        log.info("Academic year {} deleted", academicYearId);
    }

    // ========== Helper Methods ==========

    /**
     * Calculate average from subject marks
     */
    private BigDecimal calculateAverage(List<CreateTermResultRequest.SubjectMarkRequest> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double sum = subjects.stream()
                .mapToInt(CreateTermResultRequest.SubjectMarkRequest::getMark)
                .sum();

        double average = sum / subjects.size();

        return BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Map AcademicYear to response without terms
     */
    private AcademicYearResponse mapToAcademicYearResponse(AcademicYear academicYear) {
        return AcademicYearResponse.builder()
                .id(academicYear.getId())
                .year(academicYear.getYear())
                .gradeLevel(academicYear.getGradeLevel())
                .createdAt(academicYear.getCreatedAt())
                .terms(List.of())
                .build();
    }

    /**
     * Map AcademicYear to response with all terms
     */
    private AcademicYearResponse mapToAcademicYearResponseWithTerms(AcademicYear academicYear) {
        List<TermResult> termResults = termResultRepository.findByAcademicYearIdOrderByTermNumberAsc(academicYear.getId());

        List<TermResultResponse> termResponses = termResults.stream()
                .map(term -> {
                    List<SubjectMark> subjects = subjectMarkRepository.findByTermResultIdOrderBySubjectNameAsc(term.getId());
                    return mapToTermResultResponse(term, subjects);
                })
                .collect(Collectors.toList());

        return AcademicYearResponse.builder()
                .id(academicYear.getId())
                .year(academicYear.getYear())
                .gradeLevel(academicYear.getGradeLevel())
                .createdAt(academicYear.getCreatedAt())
                .terms(termResponses)
                .build();
    }

    /**
     * Map TermResult and SubjectMarks to response
     */
    private TermResultResponse mapToTermResultResponse(TermResult termResult, List<SubjectMark> subjectMarks) {
        List<SubjectMarkResponse> subjectResponses = subjectMarks.stream()
                .map(subject -> SubjectMarkResponse.builder()
                        .id(subject.getId())
                        .subjectName(subject.getSubjectName())
                        .mark(subject.getMark())
                        .build())
                .collect(Collectors.toList());

        return TermResultResponse.builder()
                .id(termResult.getId())
                .termNumber(termResult.getTermNumber())
                .averageMark(termResult.getAverageMark())
                .createdAt(termResult.getCreatedAt())
                .subjects(subjectResponses)
                .build();
    }

    /**
     * Notify all providers following this learner about result updates
     */
    private void notifyFollowersOfUpdate(Long learnerId, Long academicYearId) {
        try {
            Learner learner = learnerRepository.findById(learnerId).orElse(null);
            if (learner == null) return;

            String learnerName = learner.getFirstName() + " " + learner.getLastName();

            List<FollowResponse> followers = followService.getFollowers(learnerId);

            for (FollowResponse follower : followers) {
                notificationService.createResultUpdateNotification(
                        follower.getProviderId(),
                        learnerName,
                        academicYearId
                );
            }

            log.info("Notified {} followers about result update for learner {}", followers.size(), learnerId);
        } catch (Exception e) {
            log.error("Error notifying followers: {}", e.getMessage());
            // Don't fail the main operation if notification fails
        }
    }
}