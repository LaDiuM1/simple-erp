package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/** MVC가 실제 로그인 핸들러로 판정한 요청에만 본문 처리 전 IP rate limit을 적용한다. */
final class DemoLoginRateLimitInterceptor implements HandlerInterceptor {

    private static final String FORWARDED_FOR = "X-Forwarded-For";

    private final DemoProperties properties;
    private final DemoRateLimiter rateLimiter;

    DemoLoginRateLimitInterceptor(DemoProperties properties, DemoRateLimiter rateLimiter) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        if (!properties.isEnabled()) {
            return true;
        }

        int limit = properties.getRateLimit().getLoginLimit();
        if (!rateLimiter.tryAcquire("login", transientClientIp(request), limit)) {
            response.setHeader(
                    "Retry-After",
                    Long.toString(properties.getRateLimit().getWindow().toSeconds())
            );
            throw new BusinessException(DemoErrorCode.DEMO_RATE_LIMIT_EXCEEDED);
        }
        return true;
    }

    /** 메모리 rate limit 외 용도로 전달하지 않으며 원문을 저장·로그하지 않는다. */
    private static String transientClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(FORWARDED_FOR);
        String address = forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr()
                : forwarded.split(",", 2)[0].trim();
        if (address == null || address.isBlank()) {
            return "unknown";
        }
        return address.length() <= 128 ? address : address.substring(0, 128);
    }
}
