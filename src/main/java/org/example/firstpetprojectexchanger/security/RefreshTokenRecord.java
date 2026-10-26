package org.example.firstpetprojectexchanger.security;


public record RefreshTokenRecord(String familyId, String tokenId, Long userId, long expiresAtEpochSeconds) {

    public String asString() {
        return familyId + "." + tokenId;
    }

    public static String[] split(String token) {
        int dot = token.indexOf('.');
        if (dot < 0) {
            throw new IllegalArgumentException("Malformed refresh token");
        }
        return new String[]{token.substring(0, dot), token.substring(dot + 1)};
    }
}
