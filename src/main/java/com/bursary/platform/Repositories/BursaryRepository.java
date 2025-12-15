package com.bursary.platform.Repositories;

import com.bursary.platform.Entities.Bursary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BursaryRepository extends JpaRepository<Bursary, Long> {

    /**
     * Find all active bursaries
     */
    List<Bursary> findByIsActiveTrue();

    /**
     * Find active bursaries with deadline in the future
     */
    @Query("SELECT b FROM Bursary b WHERE b.isActive = true AND b.applicationDeadline >= :today ORDER BY b.applicationDeadline ASC")
    List<Bursary> findAvailableBursaries(@Param("today") LocalDate today);

    /**
     * Search bursaries with filters (EXACT MATCH ONLY - NO LIKE)
     */
    @Query("SELECT b FROM Bursary b JOIN b.provider p WHERE " +
            "(:minAmount IS NULL OR b.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR b.amount <= :maxAmount) AND " +
            "(:providerType IS NULL OR p.organizationType = :providerType) AND " +
            "(:location IS NULL OR p.location = :location) AND " +
            "(:isActive IS NULL OR b.isActive = :isActive) AND " +
            "(:deadlineAfter IS NULL OR b.applicationDeadline >= :deadlineAfter) AND " +
            "(:deadlineBefore IS NULL OR b.applicationDeadline <= :deadlineBefore)")
    List<Bursary> searchBursaries(
            @Param("keyword") String keyword,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("providerType") String providerType,
            @Param("location") String location,
            @Param("isActive") Boolean isActive,
            @Param("deadlineAfter") LocalDate deadlineAfter,
            @Param("deadlineBefore") LocalDate deadlineBefore
    );

    /**
     * Find bursaries by provider
     */
    List<Bursary> findByProviderId(Long providerId);

    /**
     * Count active bursaries
     */
    long countByIsActiveTrue();
}