package com.openrank.openrank.service;

import com.openrank.openrank.mapper.UserMapper;
import com.openrank.openrank.model.AuthResponse;
import com.openrank.openrank.model.LoginRequest;
import com.openrank.openrank.model.RegisterRequest;
import com.openrank.openrank.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
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
        return jwtTokenService.issueToken(userId);
    }

    public Long resolveUserId(String token) {
        return jwtTokenService.parseUserId(token);
    }

    public User findById(Long id) {
        return id == null ? null : userMapper.findById(id);
    }

    public User findByUsername(String username) {
        return username == null ? null : userMapper.findByUsername(username);
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
