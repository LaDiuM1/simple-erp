package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/** 데모 프로세스가 canonical DB·파일 세대 검증을 끝내기 전까지 쓰기를 닫아 둔다. */
@Component
@RequiredArgsConstructor
class DemoStartupVerificationGate {

    private final DemoProperties properties;
    private final AtomicBoolean seedVerified = new AtomicBoolean(false);
    private final AtomicBoolean writeReady = new AtomicBoolean(false);

    void assertWriteReady() {
        if (properties.isEnabled()
                && properties.getSeed().isValidationEnabled()
                && !writeReady.get()) {
            throw new BusinessException(DemoErrorCode.DEMO_RESET_IN_PROGRESS);
        }
    }

    void markSeedVerified() {
        seedVerified.set(true);
    }

    void openAfterStartup() {
        if (!seedVerified.get()) {
            throw new IllegalStateException("데모 seed 검증이 완료되지 않았습니다.");
        }
        writeReady.set(true);
    }
}
