package io.github.ladium1.erp.global.excel;

import java.util.List;

/**
 * 엑셀 일괄 업로드 결과 — 모든 행 검증 통과 시 전체 commit, 한 행이라도 실패 시 errors 만 채워 반환 (저장 0건).
 */
public record ExcelUploadResult(int totalRows, int successRows, int failedRows, List<ExcelRowError> errors) {

    public static ExcelUploadResult success(int totalRows) {
        return new ExcelUploadResult(totalRows, totalRows, 0, List.of());
    }

    public static ExcelUploadResult failure(int totalRows, List<ExcelRowError> errors) {
        return new ExcelUploadResult(totalRows, 0, errors.size(), errors);
    }
}
