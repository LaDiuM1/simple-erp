package io.github.ladium1.erp.global.demo;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 모든 시작 작업이 끝난 뒤 검증된 데모 프로세스의 쓰기를 연다. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "demo",
        name = {"enabled", "seed.validation-enabled"},
        havingValue = "true"
)
class DemoStartupReadyRunner implements ApplicationRunner {

    private final DemoStartupVerificationGate startupVerificationGate;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        startupVerificationGate.openAfterStartup();
    }
}
