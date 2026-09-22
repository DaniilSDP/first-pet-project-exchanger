package org.example.firstpetprojectexchanger.dto;

import org.example.firstpetprojectexchanger.model.UserRole;
import org.example.firstpetprojectexchanger.model.UserStatus;

import java.time.LocalDateTime;

public record UserDto(

        Long id,

        String email,

        String firstName,

        String lastName,

        UserRole role,

        UserStatus status,

        Integer failedLoginAttempts,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {

}







