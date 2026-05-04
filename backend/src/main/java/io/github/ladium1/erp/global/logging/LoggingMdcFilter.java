package io.github.ladium1.erp.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// 운영 로그 MDC 부착 — 모든 로그 라인에 요청 식별자 / 사용자 식별자가 자동 표기되도록.
// SecurityConfig 에서 JwtAuthenticationFilter 직후에 등록 -> 인증 결과를 MDC 로 흘려보낼 수 있음.
public class LoggingMdcFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID  = "userId";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            MDC.put(TRACE_ID, UUID.randomUUID().toString().substring(0, 8));
            putUserIdIfAuthenticated();
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private void putUserIdIfAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            MDC.put(USER_ID, authentication.getName());
        }
    }
}
