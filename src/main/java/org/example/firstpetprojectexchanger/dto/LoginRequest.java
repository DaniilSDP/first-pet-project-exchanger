package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

		@NotBlank 
		@Email 
		String email,
        
		@NotBlank 
        String password

) {
}

/*
«record»
LoginRequest
+email: String «@Email»
+password: String
 */