package com.bursary.platform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessResponse<T> {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private T data;

    public static <T> SuccessResponse<T> of(int status, String message, T data) {
        return SuccessResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> SuccessResponse<T> ok(String message, T data) {
        return of(200, message, data);
    }

    public static <T> SuccessResponse<T> created(String message, T data) {
        return of(201, message, data);
    }
}

