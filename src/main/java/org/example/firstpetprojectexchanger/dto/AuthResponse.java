package org.example.firstpetprojectexchanger.dto;

public record AuthResponse(

        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String refreshToken,
        UserDto user

) {

    public static AuthResponse of(

            String accessToken,
            String tokenType,
            long expiresInSeconds,
            String refreshToken,
            UserDto user

    ) {
        return new AuthResponse(

                accessToken,
                "Bearer",
                expiresInSeconds,
                refreshToken,
                user

        );
    }
}
/*
uthResponse

+accessToken: String
+tokenType: String
+expiresInSeconds: long
+refreshToken: String
+user: UserDto

+of(String, long, String, UserDto): AuthResponse
 */