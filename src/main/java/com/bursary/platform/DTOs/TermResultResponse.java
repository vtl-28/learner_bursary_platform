package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermResultResponse {

    private Long id;
    private Integer termNumber;
    private BigDecimal averageMark;
    private LocalDateTime createdAt;
    private List<SubjectMarkResponse> subjects;
}