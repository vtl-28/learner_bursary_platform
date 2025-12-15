package com.bursary.platform.Repositories;

import com.bursary.platform.Entities.SubjectMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectMarkRepository extends JpaRepository<SubjectMark, Long> {

    /**
     * Find all subject marks for a term result
     */
    List<SubjectMark> findByTermResultIdOrderBySubjectNameAsc(Long termResultId);

    /**
     * Delete all subject marks for a term
     */
    void deleteByTermResultId(Long termResultId);
}