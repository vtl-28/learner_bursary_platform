package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYearResponse {

    private Long id;
    private Integer year;
    private Integer gradeLevel;
    private LocalDateTime createdAt;
    private List<TermResultResponse> terms;
}