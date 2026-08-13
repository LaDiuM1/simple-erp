package io.github.ladium1.erp.global.security.internal;

import io.github.ladium1.erp.employee.api.LoginAccountApi;
import io.github.ladium1.erp.global.demo.DemoRequestGuardFilter;
import io.github.ladium1.erp.global.demo.DemoIngressGuardFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PasswordEncodingConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withPropertyValues("cors.allowed-origins=http://localhost:5173")
            .withUserConfiguration(
                    SecurityConfig.class,
                    PasswordEncodingConfiguration.class,
                    SecurityDependencies.class);

    @Test
    void creates_security_context_when_login_account_depends_on_password_encoder() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(PasswordEncoder.class);
            assertThat(context).hasSingleBean(LoginAccountApi.class);
        });
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class SecurityDependencies {

        @Bean
        JwtTokenProvider jwtTokenProvider() {
            return mock(JwtTokenProvider.class);
        }

        @Bean
        DemoRequestGuardFilter demoRequestGuardFilter() {
            return mock(DemoRequestGuardFilter.class);
        }

        @Bean
        DemoIngressGuardFilter demoIngressGuardFilter() {
            return mock(DemoIngressGuardFilter.class);
        }

        @Bean
        LoginAccountApi loginAccountApi(PasswordEncoder passwordEncoder) {
            return loginId -> true;
        }

        @Bean(name = "handlerExceptionResolver")
        HandlerExceptionResolver handlerExceptionResolver() {
            return mock(HandlerExceptionResolver.class);
        }
    }
}
