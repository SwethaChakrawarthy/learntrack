package com.learntrack.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            Base64.getEncoder().encodeToString("a-test-secret-that-is-at-least-32-bytes!".getBytes());

    @Test
    void generatedToken_canBeParsed_withSubjectAndRole() {
        JwtService jwtService = new JwtService(SECRET, 60);

        Claims claims = jwtService.parseToken(jwtService.generateToken("ana@example.com", "STUDENT"));

        assertThat(claims.getSubject()).isEqualTo("ana@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("STUDENT");
    }

    @Test
    void tokenSignedWithDifferentKey_isRejected() {
        String otherSecret = Base64.getEncoder()
                .encodeToString("another-secret-that-is-at-least-32-bytes".getBytes());
        String token = new JwtService(otherSecret, 60).generateToken("ana@example.com", "STUDENT");

        assertThatThrownBy(() -> new JwtService(SECRET, 60).parseToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expiredToken_isRejected() {
        JwtService jwtService = new JwtService(SECRET, -1); // already expired
        String token = jwtService.generateToken("ana@example.com", "STUDENT");

        assertThatThrownBy(() -> jwtService.parseToken(token))
                .isInstanceOf(JwtException.class);
    }
}
