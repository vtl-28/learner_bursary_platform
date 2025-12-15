package com.bursary.platform.Repositories;

import com.bursary.platform.Entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Find all applications by learner ID
     */
    List<Application> findByLearnerIdOrderBySubmittedAtDesc(Long learnerId);

    /**
     * Find application by learner and bursary
     */
    Optional<Application> findByLearnerIdAndBursaryId(Long learnerId, Long bursaryId);

    /**
     * Check if learner has already applied to bursary
     */
    boolean existsByLearnerIdAndBursaryId(Long learnerId, Long bursaryId);

    /**
     * Count applications by learner
     */
    long countByLearnerId(Long learnerId);

    /**
     * Find applications by status for a learner
     */
    List<Application> findByLearnerIdAndStatus(Long learnerId, String status);


    // ========== PROVIDER QUERIES ==========

    /**
     * Find all applications for bursaries belonging to a provider
     */
    @Query("SELECT a FROM Application a " +
            "JOIN a.bursary b " +
            "WHERE b.providerId = :providerId " +
            "ORDER BY a.submittedAt DESC")
    List<Application> findByProviderId(@Param("providerId") Long providerId);

    /**
     * Find applications by provider and status
     */
    @Query("SELECT a FROM Application a " +
            "JOIN a.bursary b " +
            "WHERE b.providerId = :providerId " +
            "AND a.status = :status " +
            "ORDER BY a.submittedAt DESC")
    List<Application> findByProviderIdAndStatus(@Param("providerId") Long providerId,
                                                @Param("status") String status);

    /**
     * Find applications for a specific bursary
     */
    List<Application> findByBursaryIdOrderBySubmittedAtDesc(Long bursaryId);

    /**
     * Count applications by provider
     */
    @Query("SELECT COUNT(a) FROM Application a " +
            "JOIN a.bursary b " +
            "WHERE b.providerId = :providerId")
    long countByProviderId(@Param("providerId") Long providerId);

    /**
     * Count applications by provider and status
     */
    @Query("SELECT COUNT(a) FROM Application a " +
            "JOIN a.bursary b " +
            "WHERE b.providerId = :providerId " +
            "AND a.status = :status")
    long countByProviderIdAndStatus(@Param("providerId") Long providerId,
                                    @Param("status") String status);
}