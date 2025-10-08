package com.cu2mber.gatewayservice.common.provider;

import com.cu2mber.gatewayservice.common.exception.UnauthorizedException;
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

/**
 * JWT(JSON Web Token) 토큰 검증 및 관련 로직을 제공하는 컴포넌트 클래스.
 * <p>
 * 이 클래스는 Spring Bean으로 등록되어, 다른 컴포넌트에서 JWT 토큰 검증을 수행할 수 있도록 지원합니다.
 * JWT 서명 검증, 만료 여부, 형식 오류 등을 처리하며, 문제가 있을 경우 UnauthorizedException을 발생시킵니다.
 */
@Slf4j
@Component
public class JwtProvider {

    /** application.properties 또는 application-test.properties에서 주입되는 Base64 인코딩된 시크릿 키 */
    @Value("${jwt.secret}")
    private String secret;

    /** JWT 서명용 Key 객체 */
    private Key secretKey;

    /**
     * Bean 초기화 후 실행되며, Base64로 인코딩된 secret 값을 디코딩하여 secretKey를 생성합니다.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증합니다.
     * <p>
     * 검증 실패 시 UnauthorizedException을 발생시키며, 예외 메시지는 오류 원인에 따라 다릅니다.
     *
     * @param token 검증할 JWT 토큰 문자열
     * @throws UnauthorizedException JWT가 만료되었거나 서명이 잘못되었거나 형식이 올바르지 않은 경우
     */
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

    /**
     * 토큰 값이 null이거나 공백일 경우 IllegalArgumentException을 발생시킵니다.
     *
     * @param value 검증할 토큰 문자열
     * @throws IllegalArgumentException 토큰이 null이거나 빈 문자열인 경우
     */
    private void isValidJwtToken(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT 토큰이 null이거나 비어 있습니다.");
        }
    }

    /**
     * 주어진 JWT 토큰에서 Claims 정보를 반환합니다.
     *
     * @param token JWT 토큰
     * @return Claims 토큰 내 클레임 정보
     * @throws JwtException 토큰 검증 실패 시 발생
     */
    private Claims getClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
