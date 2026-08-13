package io.github.ladium1.erp.global.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DemoFilterConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withBean(DemoProperties.class, DemoProperties::new)
            .withBean(DemoStartupVerificationGate.class, () -> mock(DemoStartupVerificationGate.class))
            .withBean(DemoProtectionPolicy.class, () -> mock(DemoProtectionPolicy.class))
            .withUserConfiguration(DemoFilterConfiguration.class);

    @Test
    void defers_exception_resolver_until_a_guarded_request_fails() {
        AtomicInteger resolverCreations = new AtomicInteger();

        contextRunner
                .withBean(
                        "handlerExceptionResolver",
                        HandlerExceptionResolver.class,
                        () -> {
                            resolverCreations.incrementAndGet();
                            return mock(HandlerExceptionResolver.class);
                        },
                        definition -> definition.setLazyInit(true)
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(DemoRequestGuardFilter.class);
                    assertThat(context).hasSingleBean(DemoIngressGuardFilter.class);
                    assertThat(resolverCreations).hasValue(0);
                });
    }
}
