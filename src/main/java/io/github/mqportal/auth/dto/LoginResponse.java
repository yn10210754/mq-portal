package io.github.mqportal.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {

    private String token;

    private String tokenType;

    private long expiresIn;

    private String username;

    private String role;

    private String displayName;
}
