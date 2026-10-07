package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateWalletRequest(

		@NotBlank
        @Pattern(regexp = "[A-Z]{3}", message = "At least 3 letters")
        @Size(min = 3, max = 3)
        String currency

) {
}
/*
CreateWalletRequest
+currency: String «@Pattern([A-Z]{3})»
 */