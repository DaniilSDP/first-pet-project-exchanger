package org.example.firstpetprojectexchanger.dto;

import org.example.firstpetprojectexchanger.model.User;

import java.time.Instant;

public record UserDto(

        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        String status,
        Instant createdAt

) {

    public static UserDto from(User user) {

        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().toString(),
                user.getStatus().toString(),
                user.getCreatedAt()
        );
    }
}

/*
«record»
UserDto

+id: Long
+email: String
+firstName: String
+lastName: String
+role: String
+status: String
+createdAt: Instant

+from(User): UserDto
 */






