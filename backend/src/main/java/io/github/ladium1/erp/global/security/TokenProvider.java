package io.github.ladium1.erp.global.security;

import org.springframework.security.core.Authentication;

public interface TokenProvider {

    // 토큰 생성
    String createToken(String loginId, String role);
    // 인증 정보 반환
    public Authentication getAuthentication(String token);
    // 토큰 검증
    public boolean validateToken(String token);

}
