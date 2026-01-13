package com.openrank.openrank.config.security;

import com.openrank.openrank.mapper.UserMapper;
import com.openrank.openrank.model.User;
import com.openrank.openrank.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 读取 Authorization Bearer Token，调用 AuthService 校验并写入 Spring Security 上下文。
 */
@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TokenAuthFilter.class);

    private final AuthService authService;
    private final UserMapper userMapper;

    public TokenAuthFilter(AuthService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (StringUtils.hasText(token)) {
                Long userId = authService.resolveUserId(token);
                if (userId != null) {
                    User user = userMapper.findById(userId);
                    if (user != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                user.getUsername(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("token 认证失败: {}", ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        String headerToken = request.getHeader("X-Auth-Token");
        if (StringUtils.hasText(headerToken)) {
            return headerToken;
        }
        return null;
    }
}
