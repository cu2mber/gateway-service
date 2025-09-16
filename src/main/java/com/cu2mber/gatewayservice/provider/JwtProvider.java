package com.cu2mber.gatewayservice.provider;

import com.cu2mber.gatewayservice.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    private Key secretKey;

/*    public JwtProvider() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }*/

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public void validateToken(String token) {
        try {
            isValidJwtToken(token);

            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            log.debug("JWT 유효성 검사 통과: {}", token);

        } catch (ExpiredJwtException e) {
            log.debug("JWT 만료. exp: {}, now: {}", e.getClaims().getExpiration(), new Date());
            throw new UnauthorizedException("만료된 JWT 토큰입니다.");
        } catch (SignatureException e) {
            log.debug("잘못된 JWT 서명. token: {}", token);
            throw new UnauthorizedException("잘못된 서명입니다.");
        } catch (MalformedJwtException e) {
            log.debug("JWT 형식 오류. token: {}", token);
            throw new UnauthorizedException("잘못된 JWT 토큰 형식입니다.");
        } catch (UnsupportedJwtException e) {
            log.debug("지원하지 않는 JWT 토큰. token: {}", token);
            throw new UnauthorizedException("지원하지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.debug("잘못된 JWT 입력. token: {}", token);
            throw new UnauthorizedException("잘못된 JWT 입력입니다.");
        } catch (JwtException e) {
            log.debug("JWT 검증 과정에서 알 수 없는 오류가 발생. message: {}, token: {}", e.getMessage(), token);
            throw new UnauthorizedException(e.getMessage());
        }
    }

    private void isValidJwtToken(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT 토큰이 null이거나 비어 있습니다.");
        }
    }

    private Claims getClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
