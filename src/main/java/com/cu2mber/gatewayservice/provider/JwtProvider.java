package com.cu2mber.gatewayservice.provider;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    private Key secretKey;

    public JwtProvider() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try {
            isValidJwtToken(token);

            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            log.debug("JWT 유효성 검사 통과: {}", token);

        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 잘못됐거나 만료된 경우
            return false;
        }
    }

    private void isValidJwtToken(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT 토큰이 null이거나 비어 있습니다.");
        }
    }
}
