package io.github.mqportal.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.mqportal.auth.JwtUtil;
import io.github.mqportal.auth.dto.LoginRequest;
import io.github.mqportal.auth.dto.LoginResponse;
import io.github.mqportal.auth.entity.User;
import io.github.mqportal.auth.mapper.UserMapper;
import io.github.mqportal.common.BizException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(401, "invalid username or password");
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs())
                .username(user.getUsername())
                .role(user.getRole())
                .displayName(user.getDisplayName())
                .build();
    }
}
