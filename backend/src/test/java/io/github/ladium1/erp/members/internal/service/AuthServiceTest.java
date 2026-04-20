package io.github.ladium1.erp.members.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.TokenProvider;
import io.github.ladium1.erp.members.internal.dto.LoginRequest;
import io.github.ladium1.erp.members.internal.dto.TokenResponse;
import io.github.ladium1.erp.members.internal.entity.Member;
import io.github.ladium1.erp.members.internal.entity.Role;
import io.github.ladium1.erp.members.internal.exception.MemberErrorCode;
import io.github.ladium1.erp.members.internal.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    private final String TEST_ID = "testId";
    private final String TEST_PASSWORD = "testPassword";

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginRequest request = new LoginRequest(TEST_ID, TEST_PASSWORD);
        Role role = Role.builder()
                .code("USER")
                .name("일반사원")
                .build();

        Member member = Member.builder()
                .loginId(TEST_ID)
                .password("encodedPassword")
                .role(role)
                .build();

        given(memberRepository.findForLoginByLoginId(TEST_ID)).willReturn(Optional.of(member));
        given(passwordEncoder.matches(TEST_PASSWORD, member.getPassword())).willReturn(true);
        given(tokenProvider.createToken(TEST_ID, "USER")).willReturn("mock.jwt.token");

        // when
        TokenResponse response = authService.login(request);

        // then
        assertNotNull(response);
        assertEquals("mock.jwt.token", response.accessToken());
    }

    @Test
    @DisplayName("직원 정보 없음")
    void login_fail_member_not_found() {
        // given
        LoginRequest request = new LoginRequest(TEST_ID, TEST_PASSWORD);
        given(memberRepository.findForLoginByLoginId(TEST_ID)).willReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("비밀번호 불일치")
    void login_fail_invalid_password() {
        // given
        LoginRequest request = new LoginRequest(TEST_ID, TEST_PASSWORD);
        Member member = Member.builder()
                .loginId(TEST_ID)
                .password("encodedPassword")
                .build();

        given(memberRepository.findForLoginByLoginId(TEST_ID)).willReturn(Optional.of(member));
        given(passwordEncoder.matches(TEST_PASSWORD, member.getPassword())).willReturn(false);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals(MemberErrorCode.INVALID_PASSWORD, exception.getErrorCode());
    }
}