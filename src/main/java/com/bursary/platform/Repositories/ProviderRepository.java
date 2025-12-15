package com.bursary.platform.Repositories;

import com.bursary.platform.Entities.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    /**
     * Find provider by email
     */
    Optional<Provider> findByEmail(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}