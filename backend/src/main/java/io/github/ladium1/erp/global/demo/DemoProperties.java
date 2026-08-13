package io.github.ladium1.erp.global.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    private Audit audit = new Audit();
    @Valid
    private Geolocation geolocation = new Geolocation();
    @Valid
    private Upload upload = new Upload();
    @Valid
    private Protection protection = new Protection();
    @Valid
    private RateLimit rateLimit = new RateLimit();
    @Valid
    private Excel excel = new Excel();
    @Valid
    private Seed seed = new Seed();

    @AssertTrue(message = "demo 모드에는 seed expected-version이 필요합니다.")
    public boolean isSeedVersionConfigured() {
        return !enabled || (seed.getExpectedVersion() != null && !seed.getExpectedVersion().isBlank());
    }

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
    public static class Audit {
        private boolean storeClientIp = true;
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
        @Min(1)
        private long accountQuotaBytes = 256L * 1024 * 1024;
        @Min(1)
        private int accountQuotaFiles = 16;
        @Min(1)
        private long generationQuotaBytes = 512L * 1024 * 1024;
        @Min(1)
        private int generationQuotaFiles = 32;
        @Min(1)
        private long minFreeBytes = 5L * 1024 * 1024 * 1024;
        @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax("1.0")
        private double minFreeRatio = 0.20;
        @Min(1)
        private int maxConcurrentTransfers = 2;
        @Min(1)
        private int maxConcurrentUploadsPerAccount = 1;
        @Min(1)
        private int maxConcurrentDownloadsPerAccount = 2;
        @Min(1)
        private int excelAccountQuotaRows = 500;
        @Min(1)
        private int excelGenerationQuotaRows = 1_000;

        @AssertTrue(message = "demo upload global quota와 transfer 한도는 account 한도 이상이어야 합니다.")
        public boolean isValidQuotaHierarchy() {
            return generationQuotaBytes >= accountQuotaBytes
                    && generationQuotaFiles >= accountQuotaFiles
                    && maxConcurrentTransfers >= maxConcurrentUploadsPerAccount
                    && maxConcurrentTransfers >= maxConcurrentDownloadsPerAccount
                    && excelGenerationQuotaRows >= excelAccountQuotaRows;
        }
    }

    @Getter
    @Setter
    public static class Protection {
        @NotEmpty
        private Set<String> protectedLoginIds = new LinkedHashSet<>(List.of("demo.manager", "demo.staff"));
        @NotEmpty
        private Set<String> protectedRoleCodes = new LinkedHashSet<>(List.of(
                "MASTER", "DEMO_MANAGER", "DEMO_STAFF"
        ));
        private String operationsAdminLoginId;
    }

    @Getter
    @Setter
    public static class Excel {
        @Min(1)
        @jakarta.validation.constraints.Max(10_000)
        private int exportMaxRows = 500;
    }

    @Getter
    @Setter
    public static class RateLimit {
        @NotNull
        private Duration window = Duration.ofMinutes(1);
        @Min(1)
        private int loginLimit = 10;
        @Min(1)
        private int loginGlobalLimit = 30;
        @Min(1)
        private int writeLimit = 60;
        @Min(1)
        private int writeGlobalLimit = 90;
        @Min(1)
        private int ingressLimit = 300;
        @Min(1)
        private int ingressGlobalLimit = 600;
        @Min(1)
        private int maxConcurrentIngress = 8;
        @Min(1)
        private int maxConcurrentWrites = 4;
        @Min(1)
        private int readLimit = 120;
        @Min(1)
        private int readGlobalLimit = 180;
        @Min(1)
        private int previewLimit = 20;
        @Min(1)
        private int previewGlobalLimit = 30;
        @Min(1)
        private int maxConcurrentReads = 4;
        @Min(1)
        private int maxConcurrentPreviews = 2;
        @Min(1)
        private int uploadLimit = 10;
        @Min(1)
        private int uploadGlobalLimit = 16;
        @Min(1)
        private int excelUploadLimit = 2;
        @Min(1)
        private int excelUploadGlobalLimit = 2;
        @Min(1)
        private int downloadLimit = 20;
        @Min(1)
        private int downloadGlobalLimit = 30;
        @NotNull
        private Duration downloadByteWindow = Duration.ofHours(1);
        @Min(1)
        private long downloadByteLimit = 64L * 1024 * 1024;
        @Min(1)
        private long downloadGlobalByteLimit = 96L * 1024 * 1024;
        @Min(1)
        private int maxTrackedKeys = 10_000;

        @AssertTrue(message = "rate-limit window는 양수여야 합니다.")
        public boolean isValidWindow() {
            return window != null
                    && window.toSeconds() >= 1
                    && downloadByteWindow != null
                    && downloadByteWindow.toSeconds() >= 1;
        }

        @AssertTrue(message = "demo global rate limit은 account limit 이상이어야 합니다.")
        public boolean isValidLimitHierarchy() {
            return loginGlobalLimit >= loginLimit
                    && ingressGlobalLimit >= ingressLimit
                    && writeGlobalLimit >= writeLimit
                    && readGlobalLimit >= readLimit
                    && previewGlobalLimit >= previewLimit
                    && uploadGlobalLimit >= uploadLimit
                    && excelUploadGlobalLimit >= excelUploadLimit
                    && downloadGlobalLimit >= downloadLimit
                    && downloadGlobalByteLimit >= downloadByteLimit;
        }
    }

    @Getter
    @Setter
    public static class Seed {
        private boolean validationEnabled = true;
        private String expectedVersion;
        @Min(1)
        private int requiredFileCount = 30;
        @NotEmpty
        private List<String> requiredAccounts = List.of(
                "demo.manager:DEMO_MANAGER",
                "demo.staff:DEMO_STAFF"
        );
        @NotEmpty
        private Set<String> requiredRoleCodes = new LinkedHashSet<>(List.of(
                "MASTER", "DEMO_MANAGER", "DEMO_STAFF"
        ));
    }
}
