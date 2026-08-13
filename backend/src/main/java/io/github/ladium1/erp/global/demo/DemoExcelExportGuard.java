package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/** 대형 entity 목록이나 POI workbook을 할당하기 전에 공개 Excel export의 DB 행 상한을 확인한다. */
@Component
public class DemoExcelExportGuard {

    private final JdbcTemplate jdbcTemplate;
    private final DemoProperties properties;

    public DemoExcelExportGuard(JdbcTemplate jdbcTemplate, DemoProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    public void assertExportAllowed(Table table) {
        if (!properties.isEnabled()) {
            return;
        }
        if (table == null) {
            throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
        }

        int limit = properties.getExcel().getExportMaxRows();
        try {
            // Filtered export도 table 전체가 안전 상한 안일 때만 허용하는 보수적 demo 경계다.
            // N+1개의 PK만 읽으므로 전체 entity/TEXT를 heap에 올리기 전에 중단한다.
            List<Long> ids = jdbcTemplate.queryForList(
                    "SELECT id FROM " + table.tableName + " ORDER BY id LIMIT ?",
                    Long.class,
                    limit + 1
            );
            if (ids.size() > limit) {
                throw new BusinessException(DemoErrorCode.DEMO_EXCEL_EXPORT_TOO_LARGE);
            }
        } catch (BusinessException guarded) {
            throw guarded;
        } catch (DataAccessException invalidPreflightState) {
            throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
        }
    }

    public enum Table {
        AFTER_SERVICES("after_services"),
        CONTRACTS("contracts"),
        CUSTOMERS("customers"),
        EMPLOYEES("employees"),
        EQUIPMENTS("equipments"),
        SALES_CONTACTS("sales_contacts");

        private final String tableName;

        Table(String tableName) {
            this.tableName = tableName;
        }
    }
}
