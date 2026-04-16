package io.github.ladium1.erp.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // 테스트용 비밀키
    private final String TEST_SECRET_KEY = "bXktc3VwZXItc2VjcmV0LXRlc3Qta2V5LWZvci1qd3QtcHJvdmlkZXItMzJieXRlcw==";

    @BeforeEach
    void setUp() {
        // minutes;
        long TEST_VALIDITY = 60L;
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET_KEY, TEST_VALIDITY);
    }

    @Test
    @DisplayName("JWT 토큰 생성")
    void createToken_Success() {
        String token = jwtTokenProvider.createToken("admin01", "ADMIN");
        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("인증 정보 추출")
    void getAuthentication_Success() {
        String token = jwtTokenProvider.createToken("user01", "ROLE_USER");
        Authentication auth = jwtTokenProvider.getAuthentication(token);

        assertThat(auth.getName()).isEqualTo("user01");
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("유효 토큰 검증")
    void validateToken_Valid() {
        String token = jwtTokenProvider.createToken("test", "ROLE_USER");
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("만료 토큰 검증")
    void validateToken_Expired() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET_KEY, -1L); // -1분으로 설정
        String token = expiredProvider.createToken("test", "ROLE_USER");
        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("잘못된 토큰 검증")
    void validateToken_Malformed() {
        String invalidToken = "invalid.token.here";
        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
    }
}