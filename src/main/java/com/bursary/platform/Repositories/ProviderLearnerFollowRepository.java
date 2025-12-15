package com.bursary.platform.Repositories;

import com.bursary.platform.Entities.ProviderLearnerFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderLearnerFollowRepository extends JpaRepository<ProviderLearnerFollow, Long> {

    /**
     * Check if provider is following learner
     */
    boolean existsByProviderIdAndLearnerId(Long providerId, Long learnerId);

    /**
     * Find follow relationship
     */
    Optional<ProviderLearnerFollow> findByProviderIdAndLearnerId(Long providerId, Long learnerId);

    /**
     * Get all learners followed by a provider
     */
    List<ProviderLearnerFollow> findByProviderIdOrderByFollowedAtDesc(Long providerId);

    /**
     * Get all providers following a learner
     */
    List<ProviderLearnerFollow> findByLearnerIdOrderByFollowedAtDesc(Long learnerId);

    /**
     * Count followers for a learner
     */
    long countByLearnerId(Long learnerId);

    /**
     * Count learners followed by provider
     */
    long countByProviderId(Long providerId);
}