package com.learntrack.course.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

/** Validates JWTs issued by auth-service. This service never issues tokens. */
@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${jwt.secret}") String base64Secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
