package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.Pattern;

public record CreateWalletRequest(

        @Pattern(regexp = "[A-Z]{3}")
        String currency

) {
}
/*
CreateWalletRequest
+currency: String «@Pattern([A-Z]{3})»
 */