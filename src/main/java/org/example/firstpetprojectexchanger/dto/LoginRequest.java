package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.Email;

public record LoginRequest(

        @Email
        String email,

        String password

) {
}

/*
«record»
LoginRequest
+email: String «@Email»
+password: String
 */