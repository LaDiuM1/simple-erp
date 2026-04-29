package io.github.ladium1.erp.global.excel;

/**
 * 엑셀 한 컬럼의 헤더 + 빌더 setter + 템플릿 가이드 메타데이터.
 * <p>
 * setter 는 trim 된 셀 문자열 (빈 문자열은 null 로 정규화) 을 받아 도메인 builder 에 적용한다.
 * 셀 파싱 / 변환 / 필드 단위 검증은 setter 안에서 수행하고, 실패 시 {@link RowParseException} 을 던진다.
 * <p>
 * required / description / allowedValues / example 는 템플릿의 "항목 안내" 시트와 열 제목 * 마킹에만 사용 — 파싱 동작에 영향 없음.
 *
 * @param <B> 도메인 builder 타입 (보통 mutable holder — 예: CustomerCreateRequest 빌더)
 */
public record ExcelImportColumn<B>(
        String header,
        boolean required,
        String description,
        /** 고정 값 enum 필드 (예: "잠재고객, 일반고객, 주요고객, 파트너"). 자유 입력이면 null. */
        String allowedValues,
        String example,
        ColumnBinder<B> binder
) {

    public void apply(B builder, String rawValue) {
        binder.accept(builder, rawValue);
    }

    /** 템플릿 시트의 헤더 표기 — required 면 "이름 *". */
    public String displayHeader() {
        return required ? header + " *" : header;
    }

    /** 자유 입력 필수 — 허용 값 없음. */
    public static <B> ExcelImportColumn<B> required(String header, String description, String example, ColumnBinder<B> binder) {
        return new ExcelImportColumn<>(header, true, description, null, example, binder);
    }

    /** 자유 입력 선택 — 허용 값 없음. */
    public static <B> ExcelImportColumn<B> optional(String header, String description, String example, ColumnBinder<B> binder) {
        return new ExcelImportColumn<>(header, false, description, null, example, binder);
    }

    /** 고정 값 enum 필수 — 허용 값을 콤마 구분 문자열로 지정. */
    public static <B> ExcelImportColumn<B> requiredEnum(String header, String description, String allowedValues, String example, ColumnBinder<B> binder) {
        return new ExcelImportColumn<>(header, true, description, allowedValues, example, binder);
    }

    /** 고정 값 enum 선택. */
    public static <B> ExcelImportColumn<B> optionalEnum(String header, String description, String allowedValues, String example, ColumnBinder<B> binder) {
        return new ExcelImportColumn<>(header, false, description, allowedValues, example, binder);
    }

    @FunctionalInterface
    public interface ColumnBinder<B> {
        /**
         * value 는 trim 된 셀 문자열. 비어 있으면 null.
         * 형식 오류 시 {@link RowParseException} 으로 필드별 에러 메시지를 알려준다.
         */
        void accept(B builder, String value);
    }
}
