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
        properties.getRateLimit().setLoginGlobalLimit(0);
        properties.getRateLimit().setWriteLimit(0);
        properties.getRateLimit().setWriteGlobalLimit(0);
        properties.getRateLimit().setIngressLimit(0);
        properties.getRateLimit().setIngressGlobalLimit(0);
        properties.getRateLimit().setMaxConcurrentIngress(0);
        properties.getRateLimit().setMaxConcurrentWrites(0);
        properties.getRateLimit().setReadLimit(0);
        properties.getRateLimit().setReadGlobalLimit(0);
        properties.getRateLimit().setPreviewLimit(0);
        properties.getRateLimit().setPreviewGlobalLimit(0);
        properties.getRateLimit().setMaxConcurrentReads(0);
        properties.getRateLimit().setMaxConcurrentPreviews(0);
        properties.getRateLimit().setUploadLimit(0);
        properties.getRateLimit().setUploadGlobalLimit(0);
        properties.getRateLimit().setExcelUploadLimit(0);
        properties.getRateLimit().setExcelUploadGlobalLimit(0);
        properties.getRateLimit().setDownloadLimit(0);
        properties.getRateLimit().setDownloadGlobalLimit(0);
        properties.getRateLimit().setDownloadByteWindow(Duration.ZERO);
        properties.getRateLimit().setDownloadByteLimit(0);
        properties.getRateLimit().setDownloadGlobalByteLimit(0);
        properties.getRateLimit().setMaxTrackedKeys(0);

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "rateLimit.validWindow",
                        "rateLimit.loginLimit",
                        "rateLimit.loginGlobalLimit",
                        "rateLimit.writeLimit",
                        "rateLimit.writeGlobalLimit",
                        "rateLimit.ingressLimit",
                        "rateLimit.ingressGlobalLimit",
                        "rateLimit.maxConcurrentIngress",
                        "rateLimit.maxConcurrentWrites",
                        "rateLimit.readLimit",
                        "rateLimit.readGlobalLimit",
                        "rateLimit.previewLimit",
                        "rateLimit.previewGlobalLimit",
                        "rateLimit.maxConcurrentReads",
                        "rateLimit.maxConcurrentPreviews",
                        "rateLimit.uploadLimit",
                        "rateLimit.uploadGlobalLimit",
                        "rateLimit.excelUploadLimit",
                        "rateLimit.excelUploadGlobalLimit",
                        "rateLimit.downloadLimit",
                        "rateLimit.downloadGlobalLimit",
                        "rateLimit.downloadByteLimit",
                        "rateLimit.downloadGlobalByteLimit",
                        "rateLimit.maxTrackedKeys"
                );
    }

    @Test
    void upload_quota_and_disk_reserve_cannot_fail_open() {
        DemoProperties properties = validEnabledProperties();
        properties.getUpload().setAccountQuotaBytes(0);
        properties.getUpload().setAccountQuotaFiles(0);
        properties.getUpload().setGenerationQuotaBytes(0);
        properties.getUpload().setGenerationQuotaFiles(0);
        properties.getUpload().setMinFreeBytes(0);
        properties.getUpload().setMinFreeRatio(0);
        properties.getUpload().setMaxConcurrentTransfers(0);
        properties.getUpload().setMaxConcurrentUploadsPerAccount(0);
        properties.getUpload().setMaxConcurrentDownloadsPerAccount(0);
        properties.getUpload().setExcelAccountQuotaRows(0);
        properties.getUpload().setExcelGenerationQuotaRows(0);

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "upload.accountQuotaBytes",
                        "upload.accountQuotaFiles",
                        "upload.generationQuotaBytes",
                        "upload.generationQuotaFiles",
                        "upload.minFreeBytes",
                        "upload.minFreeRatio",
                        "upload.maxConcurrentTransfers",
                        "upload.maxConcurrentUploadsPerAccount",
                        "upload.maxConcurrentDownloadsPerAccount",
                        "upload.excelAccountQuotaRows",
                        "upload.excelGenerationQuotaRows"
                );
    }

    @Test
    void excel_export_preflight_limit_is_bounded() {
        DemoProperties properties = validEnabledProperties();
        properties.getExcel().setExportMaxRows(0);
        assertThat(validator.validate(properties))
                .anyMatch(violation -> violation.getPropertyPath().toString()
                        .equals("excel.exportMaxRows"));

        properties.getExcel().setExportMaxRows(10_001);
        assertThat(validator.validate(properties))
                .anyMatch(violation -> violation.getPropertyPath().toString()
                        .equals("excel.exportMaxRows"));
    }

    @Test
    void global_limits_must_cover_an_account_limit() {
        DemoProperties properties = validEnabledProperties();
        properties.getUpload().setGenerationQuotaBytes(
                properties.getUpload().getAccountQuotaBytes() - 1
        );
        properties.getRateLimit().setDownloadGlobalByteLimit(
                properties.getRateLimit().getDownloadByteLimit() - 1
        );
        properties.getRateLimit().setLoginGlobalLimit(
                properties.getRateLimit().getLoginLimit() - 1
        );
        properties.getRateLimit().setReadGlobalLimit(
                properties.getRateLimit().getReadLimit() - 1
        );
        properties.getRateLimit().setIngressGlobalLimit(
                properties.getRateLimit().getIngressLimit() - 1
        );
        properties.getRateLimit().setWriteGlobalLimit(
                properties.getRateLimit().getWriteLimit() - 1
        );
        properties.getRateLimit().setPreviewGlobalLimit(
                properties.getRateLimit().getPreviewLimit() - 1
        );
        properties.getUpload().setExcelGenerationQuotaRows(
                properties.getUpload().getExcelAccountQuotaRows() - 1
        );

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("upload.validQuotaHierarchy", "rateLimit.validLimitHierarchy");
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
