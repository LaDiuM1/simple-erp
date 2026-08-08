package io.github.ladium1.erp.global.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "demo")
public class DemoProperties {

    private boolean enabled;
    @NotBlank
    private String environmentName = "DEMO";
    @NotBlank
    private String statePath = "./data/demo-state/state.json";
    @NotBlank
    private String notice = "모든 데이터는 합성 데이터이며 주기적으로 초기화됩니다.";
    @Valid
    private Reset reset = new Reset();
    @Valid
    private Geolocation geolocation = new Geolocation();
    @Valid
    private Upload upload = new Upload();

    @Getter
    @Setter
    public static class Reset {
        @NotNull
        private Duration warningBefore = Duration.ofMinutes(5);
        @NotNull
        private Duration writeLockBefore = Duration.ofMinutes(2);
        @NotNull
        private Duration operationTimeout = Duration.ofMinutes(20);

        @AssertTrue(message = "reset duration은 양수이고 write lock은 warning 이하여야 합니다.")
        public boolean isValid() {
            return warningBefore != null
                    && writeLockBefore != null
                    && operationTimeout != null
                    && warningBefore.toSeconds() >= 1
                    && writeLockBefore.toSeconds() >= 1
                    && writeLockBefore.compareTo(warningBefore) <= 0
                    && !operationTimeout.isNegative()
                    && !operationTimeout.isZero();
        }
    }

    @Getter
    @Setter
    public static class Geolocation {
        private boolean useSimulatedPosition;
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        private double latitude = 37.5663;
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        private double longitude = 126.9779;
    }

    @Getter
    @Setter
    public static class Upload {
        private boolean enabled = true;
    }

}
