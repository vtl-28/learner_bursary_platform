package com.bursary.platform.DTOs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String schoolName;
    private BigDecimal householdIncome;
    private String location;
}