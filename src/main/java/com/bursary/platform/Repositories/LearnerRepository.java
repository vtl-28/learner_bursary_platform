package com.bursary.platform.Repositories;


import com.bursary.platform.Entities.Learner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearnerRepository extends JpaRepository<Learner, Long> {

    /**
     * Find learner by email address
     * @param email the email to search for
     * @return Optional containing the learner if found
     */
    Optional<Learner> findByEmail(String email);

    /**
     * Check if email already exists in the database
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find learner by email with case-insensitive search
     * @param email the email to search for
     * @return Optional containing the learner if found
     */
    @Query("SELECT l FROM Learner l WHERE LOWER(l.email) = LOWER(:email)")
    Optional<Learner> findByEmailIgnoreCase(@Param("email") String email);


    /**
     * Find learner by ID and ensure they are active
     * @param id the learner ID
     * @return Optional containing the active learner if found
     */
//    @Query("SELECT l FROM Learner l WHERE l.id = :id AND l.isActive = true")
//    Optional<Learner> findActiveById(@Param("id") Long id);
}
