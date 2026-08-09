package io.github.ladium1.erp.global.demo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration(proxyBeanMethods = false)
public class DemoFilterConfiguration {

    @Bean
    public DemoRateLimiter demoRateLimiter(DemoProperties properties) {
        return new DemoRateLimiter(properties);
    }

    @Bean
    public DemoWebConfiguration demoWebConfiguration(
            DemoProperties properties,
            DemoRateLimiter rateLimiter
    ) {
        return new DemoWebConfiguration(properties, rateLimiter);
    }

    @Bean
    public DemoRequestGuardFilter demoRequestGuardFilter(
            DemoProperties properties,
            DemoProtectionPolicy protectionPolicy,
            DemoRateLimiter rateLimiter,
            @Qualifier("handlerExceptionResolver") ObjectProvider<HandlerExceptionResolver> exceptionResolver
    ) {
        return new DemoRequestGuardFilter(
                properties,
                protectionPolicy,
                rateLimiter,
                exceptionResolver
        );
    }

    /** SecurityFilterChain 한 곳에서만 실행되도록 servlet 자동 등록을 명시적으로 끈다. */
    @Bean
    public FilterRegistrationBean<DemoRequestGuardFilter> demoRequestGuardFilterRegistration(
            DemoRequestGuardFilter filter
    ) {
        FilterRegistrationBean<DemoRequestGuardFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
