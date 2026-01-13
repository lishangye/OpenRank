package com.openrank.openrank.model;

public record AuthResponse(
        String message,
        String token,
        String username
) {
    public static AuthResponse of(String message, String token, String username) {
        return new AuthResponse(message, token, username);
    }
}
