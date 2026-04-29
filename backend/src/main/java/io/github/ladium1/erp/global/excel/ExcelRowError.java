package io.github.ladium1.erp.global.excel;

/**
 * 엑셀 업로드 실패 행 정보 — 행 번호 (사용자 시점 = 헤더 다음 행이 1) + 컬럼 헤더 + 메시지.
 * field 가 null 이면 행 단위 검증 실패 (FK / 중복 등) 를 의미.
 */
public record ExcelRowError(int rowNum, String field, String message) {

    public static ExcelRowError of(int rowNum, String field, String message) {
        return new ExcelRowError(rowNum, field, message);
    }

    public static ExcelRowError ofRow(int rowNum, String message) {
        return new ExcelRowError(rowNum, null, message);
    }
}
