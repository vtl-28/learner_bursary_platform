package com.bursary.platform.Repositories;

import com.bursary.platform.Entities.TermResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermResultRepository extends JpaRepository<TermResult, Long> {

    /**
     * Find all term results for an academic year
     */
    List<TermResult> findByAcademicYearIdOrderByTermNumberAsc(Long academicYearId);

    /**
     * Find term result by academic year and term number
     */
    Optional<TermResult> findByAcademicYearIdAndTermNumber(Long academicYearId, Integer termNumber);

    /**
     * Check if term result exists
     */
    boolean existsByAcademicYearIdAndTermNumber(Long academicYearId, Integer termNumber);
}