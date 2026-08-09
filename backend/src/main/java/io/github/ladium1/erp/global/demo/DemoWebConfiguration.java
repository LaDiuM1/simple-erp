package io.github.ladium1.erp.global.demo;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** URI 문자열 추측 대신 MVC의 최종 handler mapping을 데모 로그인 경계로 사용한다. */
final class DemoWebConfiguration implements WebMvcConfigurer {

    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final DemoProperties properties;
    private final DemoRateLimiter rateLimiter;

    DemoWebConfiguration(DemoProperties properties, DemoRateLimiter rateLimiter) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DemoLoginRateLimitInterceptor(properties, rateLimiter))
                .addPathPatterns(LOGIN_PATH);
    }
}
