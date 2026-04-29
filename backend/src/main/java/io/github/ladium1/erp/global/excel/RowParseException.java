package io.github.ladium1.erp.global.excel;

/**
 * 엑셀 한 셀 / 한 행 파싱 중 발생하는 형식 오류.
 * field 가 null 이면 행 단위 (예: 필수 조합 누락).
 */
public class RowParseException extends RuntimeException {

    private final String field;

    public RowParseException(String message) {
        super(message);
        this.field = null;
    }

    public RowParseException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
