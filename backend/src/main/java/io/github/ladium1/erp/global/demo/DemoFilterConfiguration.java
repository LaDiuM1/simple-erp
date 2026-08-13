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
    public DemoTransferLimiter demoTransferLimiter(DemoProperties properties) {
        return new DemoTransferLimiter(properties);
    }

    @Bean
    public DemoRequestConcurrencyLimiter demoRequestConcurrencyLimiter(DemoProperties properties) {
        return new DemoRequestConcurrencyLimiter(properties);
    }

    @Bean
    public DemoIngressGuardFilter demoIngressGuardFilter(
            DemoProperties properties,
            DemoRateLimiter rateLimiter,
            DemoRequestConcurrencyLimiter requestConcurrencyLimiter,
            @Qualifier("handlerExceptionResolver") ObjectProvider<HandlerExceptionResolver> exceptionResolver
    ) {
        return new DemoIngressGuardFilter(
                properties,
                rateLimiter,
                requestConcurrencyLimiter,
                exceptionResolver
        );
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
            DemoStartupVerificationGate startupVerificationGate,
            DemoProtectionPolicy protectionPolicy,
            DemoRateLimiter rateLimiter,
            DemoTransferLimiter transferLimiter,
            DemoRequestConcurrencyLimiter requestConcurrencyLimiter,
            @Qualifier("handlerExceptionResolver") ObjectProvider<HandlerExceptionResolver> exceptionResolver
    ) {
        return new DemoRequestGuardFilter(
                properties,
                startupVerificationGate,
                protectionPolicy,
                rateLimiter,
                transferLimiter,
                requestConcurrencyLimiter,
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

    @Bean
    public FilterRegistrationBean<DemoIngressGuardFilter> demoIngressGuardFilterRegistration(
            DemoIngressGuardFilter filter
    ) {
        FilterRegistrationBean<DemoIngressGuardFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
