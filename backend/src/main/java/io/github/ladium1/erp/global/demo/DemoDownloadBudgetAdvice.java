package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 응답 직전 실제 byte 크기를 기준으로 공개 계정과 전체 다운로드 예산을 함께 제한한다.
 * controller가 byte[]를 만든 뒤 호출되므로 비용 상한을 제공하되, 단일 응답 생성의 heap/CPU 상한은
 * 요청 filter의 count와 transfer concurrency가 담당한다.
 */
@ControllerAdvice
@ConditionalOnBean(DemoProperties.class)
public class DemoDownloadBudgetAdvice implements ResponseBodyAdvice<Object> {

    private static final String GLOBAL_IDENTITY = "demo-global";

    private final DemoProperties properties;
    private final DemoRateLimiter rateLimiter;

    public DemoDownloadBudgetAdvice(DemoProperties properties, DemoRateLimiter rateLimiter) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return properties.isEnabled();
    }

    @Override
    public @Nullable Object beforeBodyWrite(
            @Nullable Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (!(body instanceof ByteArrayResource resource)) {
            return body;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            return body;
        }

        long bytes = resource.contentLength();
        DemoProperties.RateLimit limits = properties.getRateLimit();
        boolean allowed = rateLimiter.tryAcquireBoth(
                "download-bytes",
                authentication.getName(),
                limits.getDownloadByteLimit(),
                "download-bytes-global",
                GLOBAL_IDENTITY,
                limits.getDownloadGlobalByteLimit(),
                bytes,
                limits.getDownloadByteWindow()
        );
        if (!allowed) {
            response.getHeaders().set(
                    "Retry-After",
                    Long.toString(limits.getDownloadByteWindow().toSeconds())
            );
            throw new BusinessException(DemoErrorCode.DEMO_RATE_LIMIT_EXCEEDED);
        }
        return body;
    }
}
