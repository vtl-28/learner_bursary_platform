package com.bursary.platform.Repositories;

import com.bursary.platform.Entities.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    /**
     * Find all academic years for a learner
     */
    List<AcademicYear> findByLearnerIdOrderByYearDescGradeLevelDesc(Long learnerId);

    /**
     * Find academic year by learner, year, and grade
     */
    Optional<AcademicYear> findByLearnerIdAndYearAndGradeLevel(Long learnerId, Integer year, Integer gradeLevel);

    /**
     * Check if academic year exists
     */
    boolean existsByLearnerIdAndYearAndGradeLevel(Long learnerId, Integer year, Integer gradeLevel);
}