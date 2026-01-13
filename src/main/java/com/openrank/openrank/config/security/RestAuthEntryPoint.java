package com.openrank.openrank.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 未登录或会话失效时返回 401 JSON，便于前端感知。
 */
@Component
public class RestAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String message = authException == null ? "未登录或会话失效" : authException.getMessage();
        if (message == null || message.isBlank()) {
            message = "未登录或会话失效";
        }
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
        response.getWriter().write(body);
    }
}
