package io.github.ladium1.erp.global.excel;

import java.util.function.Function;

/**
 * 엑셀 한 컬럼의 헤더와 값 추출 함수.
 *
 * @param <T> 행 데이터 타입
 */
public record ExcelColumn<T>(String header, Function<T, String> extractor) {

    public String extract(T row) {
        return extractor.apply(row);
    }
}
