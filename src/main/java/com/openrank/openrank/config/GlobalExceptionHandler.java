package com.openrank.openrank.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 全局异常处理：统一返回结构，同时输出日志便于排查。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("请求地址 '{}' 参数错误：{}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().body(body(400, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String msg = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst().orElse("参数校验失败");
        log.warn("请求地址 '{}' 参数校验失败：{}", request.getRequestURI(), msg);
        return ResponseEntity.badRequest().body(body(400, msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex, HttpServletRequest request) {
        log.error("请求地址 '{}' 发生未处理异常", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(500, "服务器异常"));
    }

    private Map<String, Object> body(int code, String message) {
        return Map.of("code", code, "message", message, "data", null);
    }
}
