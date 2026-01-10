package com.openrank.openrank.model;

public record AuthResponse(
        String message,
        String token,
        String username
) {
}
