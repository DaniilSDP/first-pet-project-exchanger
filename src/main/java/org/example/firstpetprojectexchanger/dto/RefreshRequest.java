package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(

        @NotBlank
        String refreshToken

) {
}

/*
«record»
RefreshRequest
+refreshToken: String «@NotBlank»
 */