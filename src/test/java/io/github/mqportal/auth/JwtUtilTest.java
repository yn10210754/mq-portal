package io.github.mqportal.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private static final String SECRET = "01234567890123456789012345678901234567890123456789";

    @Test
    void generateAndParseRoundTrip() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 3600_000);
        String token = jwtUtil.generateToken("alice", "VIEWER");

        Claims claims = jwtUtil.parseToken(token);

        assertEquals("alice", claims.getSubject());
        assertEquals("VIEWER", claims.get("role", String.class));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 3600_000);
        String token = jwtUtil.generateToken("alice", "VIEWER");
        String tampered = token.substring(0, token.length() - 3) + "abc";

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(tampered));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, -2000);
        String token = jwtUtil.generateToken("alice", "VIEWER");

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.parseToken(token));
    }
}
