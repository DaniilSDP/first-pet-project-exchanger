package org.example.firstpetprojectexchanger.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(

        Instant timestamp,

        int status,

        String error,

        String message,

        Map<String, String> fieldErrors

) {

    public static ErrorResponse of(
            int status,
            String error,
            String message
    ) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                Map.of()
        );
    }

    public static ErrorResponse of(
            int status,
            String error,
            String message,
            Map<String, String> fieldErrors
    ) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                fieldErrors
        );
    }
}

/*
ErrorResponse

+timestamp: Instant
+status: int
+error: String
+message: String
+fieldErrors: Map<String,String>

+of(int, String, String): ErrorResponse
+of(int, String, String, Map): ErrorResponse
 */