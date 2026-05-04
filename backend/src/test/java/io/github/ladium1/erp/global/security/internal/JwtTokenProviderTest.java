package io.github.ladium1.erp.global.security.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private final String TEST_SECRET_KEY = "bXktc3VwZXItc2VjcmV0LXRlc3Qta2V5LWZvci1qd3QtcHJvdmlkZXItMzJieXRlcw==";

    @BeforeEach
    void setUp() {
        long TEST_VALIDITY = 60L;   // minutes
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET_KEY, TEST_VALIDITY);
    }

    @Test
    @DisplayName("토큰 생성 성공")
    void create_token_success() {
        // when
        String token = jwtTokenProvider.createToken("admin01", "ADMIN");

        // then
        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("인증 정보 추출 성공")
    void get_authentication_success() {
        // given
        String token = jwtTokenProvider.createToken("user01", "ROLE_USER");

        // when
        Authentication auth = jwtTokenProvider.getAuthentication(token);

        // then
        assertThat(auth.getName()).isEqualTo("user01");
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("유효 토큰 검증 성공")
    void validate_token_valid() {
        // given
        String token = jwtTokenProvider.createToken("test", "ROLE_USER");

        // when & then
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("만료 토큰 검증 실패")
    void validate_token_expired() {
        // given
        JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET_KEY, -1L);   // -1분
        String token = expiredProvider.createToken("test", "ROLE_USER");

        // when & then
        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 실패")
    void validate_token_malformed() {
        // when & then
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }
}
