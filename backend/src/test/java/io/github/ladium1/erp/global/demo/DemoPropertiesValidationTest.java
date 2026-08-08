package io.github.ladium1.erp.global.demo;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DemoPropertiesValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

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
        properties.getReset().setWarningBefore(Duration.ofMillis(500));
        properties.getReset().setWriteLockBefore(Duration.ofMillis(500));

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("reset.valid");
    }

    private static DemoProperties validEnabledProperties() {
        DemoProperties properties = new DemoProperties();
        properties.setEnabled(true);
        return properties;
    }
}
