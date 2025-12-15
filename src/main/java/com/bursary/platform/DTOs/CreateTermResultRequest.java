package com.bursary.platform.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTermResultRequest {

    @NotNull(message = "Term number is required")
    @Min(value = 1, message = "Term number must be between 1 and 4")
    @Max(value = 4, message = "Term number must be between 1 and 4")
    private Integer termNumber;

    @NotEmpty(message = "At least one subject is required")
    @Valid
    private List<SubjectMarkRequest> subjects;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectMarkRequest {

        @NotNull(message = "Subject name is required")
        private String subjectName;

        @NotNull(message = "Mark is required")
        @Min(value = 0, message = "Mark must be between 0 and 100")
        @Max(value = 100, message = "Mark must be between 0 and 100")
        private Integer mark;
    }
}