package io.github.ladium1.erp.global.demo;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DemoPropertiesValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void enabled_demo_requires_seed_version() {
        DemoProperties properties = validEnabledProperties();
        properties.getSeed().setExpectedVersion(" ");

        assertThat(validator.validate(properties))
                .anyMatch(violation -> violation.getPropertyPath().toString()
                        .equals("seedVersionConfigured"));
    }

    @Test
    void rate_limit_cannot_fail_open_with_zero_values() {
        DemoProperties properties = validEnabledProperties();
        properties.getRateLimit().setWindow(Duration.ZERO);
        properties.getRateLimit().setLoginLimit(0);
        properties.getRateLimit().setWriteLimit(0);
        properties.getRateLimit().setMaxTrackedKeys(0);

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "rateLimit.validWindow",
                        "rateLimit.loginLimit",
                        "rateLimit.writeLimit",
                        "rateLimit.maxTrackedKeys"
                );
    }

    @Test
    void reset_boundaries_must_be_positive_and_ordered() {
        DemoProperties properties = validEnabledProperties();
        properties.getReset().setWarningBefore(Duration.ofMinutes(1));
        properties.getReset().setWriteLockBefore(Duration.ofMinutes(2));

        assertThat(validator.validate(properties))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("reset.valid"));
    }

    @Test
    void subsecond_durations_that_would_emit_zero_seconds_are_rejected() {
        DemoProperties properties = validEnabledProperties();
        properties.getRateLimit().setWindow(Duration.ofMillis(500));
        properties.getReset().setWarningBefore(Duration.ofMillis(500));
        properties.getReset().setWriteLockBefore(Duration.ofMillis(500));

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("rateLimit.validWindow", "reset.valid");
    }

    @Test
    void canonical_file_count_must_be_positive() {
        DemoProperties properties = validEnabledProperties();
        properties.getSeed().setRequiredFileCount(0);

        assertThat(validator.validate(properties))
                .anyMatch(violation -> violation.getPropertyPath().toString()
                        .equals("seed.requiredFileCount"));
    }

    private static DemoProperties validEnabledProperties() {
        DemoProperties properties = new DemoProperties();
        properties.setEnabled(true);
        properties.getSeed().setExpectedVersion("test-seed");
        return properties;
    }
}
