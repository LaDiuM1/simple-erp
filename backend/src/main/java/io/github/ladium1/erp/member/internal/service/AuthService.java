package io.github.ladium1.erp.member.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.TokenProvider;
import io.github.ladium1.erp.member.internal.dto.LoginRequest;
import io.github.ladium1.erp.member.internal.dto.TokenResponse;
import io.github.ladium1.erp.member.internal.entity.Member;
import io.github.ladium1.erp.member.internal.exception.MemberErrorCode;
import io.github.ladium1.erp.member.internal.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findForLoginByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(MemberErrorCode.INVALID_PASSWORD);
        }

        String token = tokenProvider.createToken(member.getLoginId(), member.getRole().getCode());

        return new TokenResponse(token);
    }
}