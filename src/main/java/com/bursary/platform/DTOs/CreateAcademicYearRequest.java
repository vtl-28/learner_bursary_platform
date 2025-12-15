package com.bursary.platform.DTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAcademicYearRequest {

    @NotNull(message = "Year is required")
    @Min(value = 2020, message = "Year must be 2020 or later")
    @Max(value = 2030, message = "Year must be 2030 or earlier")
    private Integer year;

    @NotNull(message = "Grade level is required")
    @Min(value = 8, message = "Grade level must be between 8 and 12")
    @Max(value = 12, message = "Grade level must be between 8 and 12")
    private Integer gradeLevel;
}