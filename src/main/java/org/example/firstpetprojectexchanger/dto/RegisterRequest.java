package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(

        String firstName,

        String lastName,
        
        @NotBlank
        @Email
        @Max(255)
        String email,
        
        @NotBlank
        @Min(8)
        @Max(72)
        String password

) {
}

/*
«record»
RegisterRequest

+firstName: String
+lastName: String
+email: String «@Email @Size(max=255)»
+password: String «@Size(min=8,max=72)»
 */