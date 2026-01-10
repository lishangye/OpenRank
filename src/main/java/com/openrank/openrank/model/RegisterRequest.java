package com.openrank.openrank.model;

public record RegisterRequest(
        String username,
        String password,
        String email
) {
}
