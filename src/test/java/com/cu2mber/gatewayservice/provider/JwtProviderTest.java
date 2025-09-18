package com.cu2mber.gatewayservice.provider;

import com.cu2mber.gatewayservice.common.exception.UnauthorizedException;
import com.cu2mber.gatewayservice.common.provider.JwtProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {
    private JwtProvider jwtProvider;
    private Key secretkey;
    private String validToken;
    private String expiredToken;
    private String malformedToken = "I.Am.Not.A.JWT.Token.Type.Token.";
    private final String TEST_USER= "test-user";
    private final String anotherKey = "aWFtYW5vdGhlcnRlc3RzZWNyZXRrZXkxMjM0NTY3ODkwYWJjZGVm";  // 'iamanothertestsecretkey1234567890abcdef'를 BASE64로 인코딩

    @BeforeEach
    void setUP() {
        jwtProvider = new JwtProvider();

        // 테스트용 시크릿키 생성
        String secret = "aWFtdGVzdHNlY3JldGtleTEyMzQ1Njc4OTBhYmNkZWY="; // 'iamtestsecretkey1234567890abcdef'를 BASE64로 인코딩
        ReflectionTestUtils.setField(jwtProvider, "secret", secret);
        jwtProvider.init();

        byte[] secretBytes = java.util.Base64.getDecoder().decode(secret);
        secretkey = Keys.hmacShaKeyFor(secretBytes);

        // 유효한 토큰 생성
        validToken = Jwts.builder()
                .setSubject(TEST_USER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60)) // 1시간 후
                .signWith(secretkey, SignatureAlgorithm.HS256)
                .compact();

        // 만료된 토큰 생성
        expiredToken = Jwts.builder()
                .setSubject(TEST_USER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60)) // 1분 전
                .signWith(secretkey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    @DisplayName("유효한 토큰은 예외 없이 통과")
    void validateToken_validToken() {
        assertDoesNotThrow(() -> jwtProvider.validateToken(validToken));
    }

    @Test
    @DisplayName("만료된 토큰의 경우 예외 발생")
    void validateToken_ExpiredJwtException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> jwtProvider.validateToken(expiredToken));

        assertEquals("만료된 JWT 토큰입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("토큰의 형식이 잘못된 경우 예외 발생")
    void validateToken_MalformedJwtException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> jwtProvider.validateToken(malformedToken));

        assertEquals("잘못된 JWT 토큰 형식입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("지원하지 않는 토큰의 경우 예외 발생")
    void validateToken_expiredToken() {
        // alg: none으로 서명한 토큰 (지원되지 않는 알고리즘)
        String unsupportedToken = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJ0ZXN0In0.";

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> jwtProvider.validateToken(unsupportedToken));

        assertEquals("지원하지 않는 JWT 토큰입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("JWT 입력이 null일 경우 예외 발생")
    void validateToken_IllegalArgumentException_null() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> jwtProvider.validateToken(null));

        assertEquals("잘못된 JWT 입력입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("JWT 입력이 빈 문자열일 경우 예외 발생")
    void validateToken_IllegalArgumentException_empty() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> jwtProvider.validateToken(" "));

        assertEquals("잘못된 JWT 입력입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("토큰의 서명이 잘못된 경우 예외 발생")
    void validateToken_SignatureException() {
        byte[] anotherKeyBytes = java.util.Base64.getDecoder().decode(anotherKey);
        Key otherKey = Keys.hmacShaKeyFor(anotherKeyBytes);

        String tokenWithWrongSignature = Jwts.builder()
                .setSubject(TEST_USER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(otherKey, SignatureAlgorithm.HS256)
                .compact();

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> jwtProvider.validateToken(tokenWithWrongSignature));

        assertEquals("잘못된 서명입니다.", exception.getMessage());
    }
}