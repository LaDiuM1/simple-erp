package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemoExcelExportGuardTest {

    private DemoProperties properties;
    private JdbcTemplate jdbcTemplate;
    private DemoExcelExportGuard guard;

    @BeforeEach
    void setUp() {
        properties = new DemoProperties();
        properties.setEnabled(true);
        properties.getExcel().setExportMaxRows(500);

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:demo-excel-export-" + UUID.randomUUID()
                + ";MODE=MariaDB;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE after_services (id BIGINT PRIMARY KEY, symptom CLOB)");
        guard = new DemoExcelExportGuard(jdbcTemplate, properties);
    }

    @Test
    @DisplayName("정확히 500행은 entity나 TEXT를 읽지 않는 PK preflight에서 허용")
    void exact_five_hundred_rows_are_allowed() {
        insertRows(500);

        guard.assertExportAllowed(DemoExcelExportGuard.Table.AFTER_SERVICES);
    }

    @Test
    @DisplayName("501번째 PK가 있으면 대형 목록 materialization 전에 stable 429로 거절")
    void five_hundred_and_first_row_is_rejected() {
        insertRows(501);

        assertThatThrownBy(() ->
                guard.assertExportAllowed(DemoExcelExportGuard.Table.AFTER_SERVICES))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        DemoErrorCode.DEMO_EXCEL_EXPORT_TOO_LARGE
                );
    }

    @Test
    @DisplayName("preflight DB 상태를 확인할 수 없으면 export를 허용하지 않음")
    void database_failure_is_fail_closed() {
        assertThatThrownBy(() ->
                guard.assertExportAllowed(DemoExcelExportGuard.Table.CONTRACTS))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        DemoErrorCode.DEMO_STORAGE_UNAVAILABLE
                );
    }

    @Test
    @DisplayName("demo 비활성 환경은 product 기능을 변경하지 않고 DB preflight도 수행하지 않음")
    void disabled_demo_does_not_query() {
        properties.setEnabled(false);

        guard.assertExportAllowed(DemoExcelExportGuard.Table.CONTRACTS);
    }

    private void insertRows(int count) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO after_services(id, symptom) VALUES (?, ?)",
                java.util.stream.IntStream.rangeClosed(1, count)
                        .mapToObj(id -> new Object[]{(long) id, "x".repeat(4000)})
                        .toList()
        );
    }
}
