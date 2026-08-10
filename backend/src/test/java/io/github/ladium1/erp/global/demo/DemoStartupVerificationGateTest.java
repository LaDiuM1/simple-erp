package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import org.springframework.boot.ApplicationArguments;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class DemoStartupVerificationGateTest {

    @Test
    @DisplayName("demo startup 검증 전에는 쓰기를 503 안정 코드로 차단하고 완료 후 허용")
    void blocks_until_canonical_verification_finishes() {
        DemoProperties properties = new DemoProperties();
        properties.setEnabled(true);
        DemoStartupVerificationGate gate = new DemoStartupVerificationGate(properties);

        assertThatThrownBy(gate::assertWriteReady)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DemoErrorCode.DEMO_RESET_IN_PROGRESS);

        gate.markSeedVerified();

        assertThatThrownBy(gate::assertWriteReady)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DemoErrorCode.DEMO_RESET_IN_PROGRESS);

        gate.openAfterStartup();

        assertThatCode(gate::assertWriteReady).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("마지막 시작 작업은 seed 검증이 끝나지 않았으면 쓰기를 열지 않음")
    void startup_completion_cannot_open_before_seed_verification() {
        DemoProperties properties = new DemoProperties();
        properties.setEnabled(true);
        DemoStartupVerificationGate gate = new DemoStartupVerificationGate(properties);

        assertThatThrownBy(gate::openAfterStartup)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seed 검증");
        assertThatThrownBy(gate::assertWriteReady)
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("마지막 시작 runner가 실행된 뒤에만 검증된 데모 쓰기를 허용")
    void final_startup_runner_opens_verified_gate() throws Exception {
        DemoProperties properties = new DemoProperties();
        properties.setEnabled(true);
        DemoStartupVerificationGate gate = new DemoStartupVerificationGate(properties);
        gate.markSeedVerified();

        new DemoStartupReadyRunner(gate).run(mock(ApplicationArguments.class));

        assertThatCode(gate::assertWriteReady).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("demo off에서는 startup gate가 일반 환경 쓰기에 관여하지 않음")
    void demo_off_is_always_open() {
        DemoProperties properties = new DemoProperties();
        properties.setEnabled(false);

        assertThatCode(new DemoStartupVerificationGate(properties)::assertWriteReady)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("seed 검증을 명시적으로 끈 데모에서는 startup gate가 기존 동작을 보존")
    void validation_disabled_is_open() {
        DemoProperties properties = new DemoProperties();
        properties.setEnabled(true);
        properties.getSeed().setValidationEnabled(false);

        assertThatCode(new DemoStartupVerificationGate(properties)::assertWriteReady)
                .doesNotThrowAnyException();
    }
}
