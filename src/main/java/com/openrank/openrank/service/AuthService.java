package com.openrank.openrank.service;

import com.openrank.openrank.mapper.UserMapper;
import com.openrank.openrank.model.AuthResponse;
import com.openrank.openrank.model.LoginRequest;
import com.openrank.openrank.model.RegisterRequest;
import com.openrank.openrank.model.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String SESSION_PREFIX = "session:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    public AuthService(UserMapper userMapper, StringRedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    public AuthResponse register(RegisterRequest request) {
        validateRegister(request);

        User existing = userMapper.findByUsername(request.username());
        if (existing != null) {
            return new AuthResponse("用户名已存在", null, null);
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insertUser(user);

        String token = issueToken(user.getId());
        return new AuthResponse("注册成功", token, user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        if (!StringUtils.hasText(request.username()) || !StringUtils.hasText(request.password())) {
            return new AuthResponse("用户名或密码不能为空", null, null);
        }

        User user = userMapper.findByUsername(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return new AuthResponse("用户名或密码错误", null, null);
        }

        String token = issueToken(user.getId());
        return new AuthResponse("登录成功", token, user.getUsername());
    }

    private String issueToken(Long userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(SESSION_PREFIX + token, String.valueOf(userId), SESSION_TTL);
        return token;
    }

    public Long resolveUserId(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        String value = redisTemplate.opsForValue().get(SESSION_PREFIX + token);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        redisTemplate.expire(SESSION_PREFIX + token, SESSION_TTL);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void validateRegister(RegisterRequest request) {
        if (!StringUtils.hasText(request.username())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.password())) {
            throw new IllegalArgumentException("密码不能为空");
        }
    }
}
