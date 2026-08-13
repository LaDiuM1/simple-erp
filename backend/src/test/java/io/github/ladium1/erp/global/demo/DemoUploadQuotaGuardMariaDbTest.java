package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "DEMO_MARIADB_TEST_URL", matches = ".+")
class DemoUploadQuotaGuardMariaDbTest {

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
    private DemoUploadQuotaGuard guard;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
        dataSource.setUrl(System.getenv("DEMO_MARIADB_TEST_URL"));
        dataSource.setUsername(System.getenv().getOrDefault("DEMO_MARIADB_TEST_USER", "root"));
        dataSource.setPassword(System.getenv().getOrDefault("DEMO_MARIADB_TEST_PASSWORD", "demo-root"));
        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("DROP TABLE IF EXISTS stored_files");
        jdbcTemplate.execute("DROP TABLE IF EXISTS drive_folders");
        jdbcTemplate.execute("DROP TABLE IF EXISTS demo_seed_manifest");
        jdbcTemplate.execute("CREATE TABLE demo_seed_manifest (id TINYINT PRIMARY KEY, reset_at DATETIME(6) NOT NULL) ENGINE=InnoDB");
        jdbcTemplate.execute("CREATE TABLE drive_folders (id BIGINT PRIMARY KEY) ENGINE=InnoDB");
        jdbcTemplate.execute("""
                CREATE TABLE stored_files (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    created_at DATETIME(6), uploader_id BIGINT, size BIGINT NOT NULL
                ) ENGINE=InnoDB
                """);
        jdbcTemplate.update("INSERT INTO demo_seed_manifest(id, reset_at) VALUES (1, '2026-08-13 09:00:00.000000')");
        jdbcTemplate.update("INSERT INTO drive_folders(id) VALUES (1)");

        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        DemoProperties properties = new DemoProperties();
        properties.setEnabled(true);
        properties.getUpload().setAccountQuotaBytes(10);
        properties.getUpload().setAccountQuotaFiles(16);
        properties.getUpload().setGenerationQuotaBytes(10);
        properties.getUpload().setGenerationQuotaFiles(32);
        properties.getUpload().setMinFreeBytes(1);
        properties.getUpload().setMinFreeRatio(0.01);
        guard = new DemoUploadQuotaGuard(
                jdbcTemplate,
                properties,
                ignored -> new DemoUploadQuotaGuard.DiskSpace(1_000, 1_000)
        );
    }

    @AfterEach
    void tearDown() {
        if (jdbcTemplate != null) {
            jdbcTemplate.execute("DROP TABLE IF EXISTS stored_files");
            jdbcTemplate.execute("DROP TABLE IF EXISTS drive_folders");
            jdbcTemplate.execute("DROP TABLE IF EXISTS demo_seed_manifest");
        }
    }

    @Test
    @DisplayName("MariaDB READ COMMITTED의 prior read 뒤 manifest 대기는 직전 upload를 관찰")
    void locking_usage_read_sees_commit_after_prior_read() throws Exception {
        CountDownLatch winnerHasManifestLock = new CountDownLatch(1);
        CountDownLatch contenderHasOldSnapshot = new CountDownLatch(1);
        AtomicReference<Throwable> winnerFailure = new AtomicReference<>();
        AtomicReference<Throwable> contenderResult = new AtomicReference<>();

        try (var executor = Executors.newFixedThreadPool(2)) {
            executor.submit(() -> {
                try {
                    transactionTemplate.executeWithoutResult(ignored -> {
                        guard.assertUploadAllowed(1L, 6, Path.of("/demo/files"));
                        winnerHasManifestLock.countDown();
                        await(contenderHasOldSnapshot);
                        jdbcTemplate.update("""
                                INSERT INTO stored_files(created_at, uploader_id, size)
                                VALUES ('2026-08-13 09:01:00.000000', 1, 6)
                                """);
                    });
                } catch (Throwable failure) {
                    winnerFailure.set(failure);
                }
            });
            executor.submit(() -> {
                try {
                    assertThat(winnerHasManifestLock.await(5, TimeUnit.SECONDS)).isTrue();
                    transactionTemplate.executeWithoutResult(ignored -> {
                        // JPA folder lookup과 같은 prior read 뒤에도 RC의 다음 statement는 최신 commit을 본다.
                        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM drive_folders", Long.class);
                        contenderHasOldSnapshot.countDown();
                        guard.assertUploadAllowed(1L, 6, Path.of("/demo/files"));
                    });
                } catch (Throwable result) {
                    contenderResult.set(result);
                }
            });
            executor.shutdown();
            assertThat(executor.awaitTermination(15, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(winnerFailure.get()).isNull();
        assertThat(contenderResult.get()).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) contenderResult.get()).getErrorCode())
                .isEqualTo(DemoErrorCode.DEMO_UPLOAD_QUOTA_EXCEEDED);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stored_files", Long.class))
                .isEqualTo(1L);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("concurrency coordination timed out");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("concurrency coordination interrupted", interrupted);
        }
    }
}
