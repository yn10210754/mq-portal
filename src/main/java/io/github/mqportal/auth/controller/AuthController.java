package io.github.mqportal.auth.controller;

import io.github.mqportal.auth.dto.LoginRequest;
import io.github.mqportal.auth.dto.LoginResponse;
import io.github.mqportal.auth.service.AuthService;
import io.github.mqportal.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", authentication == null ? null : authentication.getName());
        List<String> roles = authentication == null ? List.of()
                : authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        body.put("roles", roles);
        return Result.ok(body);
    }
}
