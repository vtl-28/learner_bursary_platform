package com.bursary.platform.Services;

import com.bursary.platform.DTOs.BursaryDetailResponse;
import com.bursary.platform.DTOs.BursarySearchRequest;
import com.bursary.platform.DTOs.BursarySummaryResponse;
import com.bursary.platform.Entities.Bursary;
import com.bursary.platform.Entities.Provider;
import com.bursary.platform.Exceptions.ResourceNotFoundException;
import com.bursary.platform.Repositories.BursaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BursaryService {

    private final BursaryRepository bursaryRepository;

    /**
     * Get all active bursaries
     */
    @Transactional(readOnly = true)
    public List<BursarySummaryResponse> getAllActiveBursaries() {
        log.info("Fetching all active bursaries");

        List<Bursary> bursaries = bursaryRepository.findByIsActiveTrue();

        return bursaries.stream()
                .map(this::mapToSummary)
                .sorted(Comparator.comparing(BursarySummaryResponse::getApplicationDeadline))
                .collect(Collectors.toList());
    }

    /**
     * Get all available bursaries (active + deadline not passed)
     */
    @Transactional(readOnly = true)
    public List<BursarySummaryResponse> getAvailableBursaries() {
        log.info("Fetching available bursaries");

        List<Bursary> bursaries = bursaryRepository.findAvailableBursaries(LocalDate.now());

        return bursaries.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    /**
     * Search bursaries with filters
     */
    @Transactional(readOnly = true)
    public List<BursarySummaryResponse> searchBursaries(BursarySearchRequest searchRequest) {
        log.info("Searching bursaries with filters: {}", searchRequest);

        // Log each parameter to see which one is causing issues
        log.debug("keyword: {}, type: {}", searchRequest.getKeyword(),
                searchRequest.getKeyword() != null ? searchRequest.getKeyword().getClass() : "null");
        log.debug("providerType: {}, type: {}", searchRequest.getProviderType(),
                searchRequest.getProviderType() != null ? searchRequest.getProviderType().getClass() : "null");
        log.debug("location: {}, type: {}", searchRequest.getLocation(),
                searchRequest.getLocation() != null ? searchRequest.getLocation().getClass() : "null");

        // Default isActive to true if not specified
        Boolean isActive = searchRequest.getIsActive() != null ? searchRequest.getIsActive() : true;

        try {
            List<Bursary> bursaries = bursaryRepository.searchBursaries(
                    searchRequest.getKeyword(),
                    searchRequest.getMinAmount(),
                    searchRequest.getMaxAmount(),
                    searchRequest.getProviderType(),
                    searchRequest.getLocation(),
                    isActive,
                    searchRequest.getDeadlineAfter(),
                    searchRequest.getDeadlineBefore()
            );

            // Apply sorting
            List<BursarySummaryResponse> results = bursaries.stream()
                    .map(this::mapToSummary)
                    .collect(Collectors.toList());

            return applySorting(results, searchRequest.getSortBy(), searchRequest.getSortDirection());

        } catch (Exception e) {
            log.error("Error during bursary search. Request: {}", searchRequest, e);
            throw e; // Re-throw to see full stack trace
        }
    }

    /**
     * Get bursary details by ID
     */
    @Transactional(readOnly = true)
    public BursaryDetailResponse getBursaryById(Long id) {
        log.info("Fetching bursary details for ID: {}", id);

        Bursary bursary = bursaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bursary not found with ID: " + id));

        return mapToDetail(bursary);
    }

    /**
     * Get total count of active bursaries
     */
    @Transactional(readOnly = true)
    public long getActiveBursaryCount() {
        return bursaryRepository.countByIsActiveTrue();
    }

    // ========== Helper Methods ==========

    private BursarySummaryResponse mapToSummary(Bursary bursary) {
        Provider provider = bursary.getProvider();

        return BursarySummaryResponse.builder()
                .id(bursary.getId())
                .title(bursary.getTitle())
                .amount(bursary.getAmount())
                .applicationDeadline(bursary.getApplicationDeadline())
                .providerName(provider != null ? provider.getOrganizationName() : "Unknown")
                .providerType(provider != null ? provider.getOrganizationType() : null)
                .providerLocation(provider != null ? provider.getLocation() : null)
                .isActive(bursary.getIsActive())
                .isAvailable(bursary.isAvailable())
                .build();
    }

    private BursaryDetailResponse mapToDetail(Bursary bursary) {
        Provider provider = bursary.getProvider();

        BursaryDetailResponse.ProviderInfo providerInfo = null;
        if (provider != null) {
            providerInfo = BursaryDetailResponse.ProviderInfo.builder()
                    .id(provider.getId())
                    .organizationName(provider.getOrganizationName())
                    .organizationType(provider.getOrganizationType())
                    .location(provider.getLocation())
                    .build();
        }

        return BursaryDetailResponse.builder()
                .id(bursary.getId())
                .title(bursary.getTitle())
                .description(bursary.getDescription())
                .amount(bursary.getAmount())
                .applicationDeadline(bursary.getApplicationDeadline())
                .isActive(bursary.getIsActive())
                .criteria(bursary.getCriteria())
                .createdAt(bursary.getCreatedAt())
                .provider(providerInfo)
                .build();
    }

    private List<BursarySummaryResponse> applySorting(List<BursarySummaryResponse> bursaries,
                                                      String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) {
            return bursaries; // Return as-is
        }

        Comparator<BursarySummaryResponse> comparator;

        switch (sortBy.toLowerCase()) {
            case "amount":
                comparator = Comparator.comparing(BursarySummaryResponse::getAmount);
                break;
            case "deadline":
                comparator = Comparator.comparing(BursarySummaryResponse::getApplicationDeadline);
                break;
            default:
                return bursaries; // Invalid sort field, return as-is
        }

        // Apply sort direction
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return bursaries.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}