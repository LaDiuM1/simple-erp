package io.github.ladium1.erp.global.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * MultipartFile + 컬럼 정의를 받아 도메인 builder 리스트로 파싱.
 * <p>
 * 헤더 행은 첫 번째 row 에서 컬럼 정의 순서대로 일치해야 하며, 빈 행은 자동 skip.
 * 컬럼별 setter 가 던진 {@link RowParseException} 은 에러로 수집되고, 한 셀이 실패해도 같은 행의 나머지 셀은 계속 파싱.
 */
@Component
public class ExcelImporter {

    private static final DataFormatter FORMATTER = new DataFormatter();
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public <B> ParsedRows<B> parse(MultipartFile file,
                                   List<ExcelImportColumn<B>> columns,
                                   Supplier<B> builderFactory) {
        try (InputStream input = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(input)) {

            Sheet sheet = workbook.getSheetAt(0);
            validateHeader(sheet, columns);

            List<ParsedRow<B>> builders = new ArrayList<>();
            List<ExcelRowError> errors = new ArrayList<>();
            int totalRows = 0;

            int lastRowNum = sheet.getLastRowNum();
            for (int rowIdx = 1; rowIdx <= lastRowNum; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (isBlankRow(row, columns.size())) continue;
                totalRows++;
                int userRowNum = rowIdx; // 헤더 다음 첫 데이터 행이 1.

                B builder = builderFactory.get();
                boolean rowHasError = false;
                for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
                    ExcelImportColumn<B> column = columns.get(colIdx);
                    String raw = readCellAsString(row.getCell(colIdx));
                    try {
                        column.apply(builder, raw);
                    } catch (RowParseException e) {
                        errors.add(ExcelRowError.of(userRowNum, column.header(), e.getMessage()));
                        rowHasError = true;
                    }
                }
                if (!rowHasError) {
                    builders.add(new ParsedRow<>(userRowNum, builder));
                }
            }
            return new ParsedRows<>(totalRows, builders, errors);
        } catch (IOException e) {
            throw new IllegalArgumentException("엑셀 파일을 열 수 없습니다.", e);
        }
    }

    private <B> void validateHeader(Sheet sheet, List<ExcelImportColumn<B>> columns) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalArgumentException("엑셀 첫 행에 열 제목이 없습니다.");
        }
        for (int i = 0; i < columns.size(); i++) {
            String expected = columns.get(i).header();
            String actual = normalizeHeader(readCellAsString(header.getCell(i)));
            if (!expected.equals(actual)) {
                throw new IllegalArgumentException(
                        "엑셀 열 제목이 올바르지 않습니다. " + (i + 1) + "번째 항목은 '" + expected + "' 이어야 합니다."
                );
            }
        }
    }

    /**
     * 템플릿 열 제목의 trailing `*` 마킹 / 좌우 공백을 무시 — 다운로드(label) 와 템플릿(label *) 양쪽 호환.
     * 사용자가 실수로 `*` 를 지워도 round-trip 이 깨지지 않음.
     */
    private static String normalizeHeader(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        while (trimmed.endsWith("*")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private boolean isBlankRow(Row row, int columnCount) {
        if (row == null) return true;
        for (int i = 0; i < columnCount; i++) {
            String value = readCellAsString(row.getCell(i));
            if (value != null && !value.isEmpty()) return false;
        }
        return true;
    }

    /**
     * 셀 값 → trim 된 string. NUMERIC 셀은 정수면 소수점 없이 출력.
     * 빈 문자열은 null 로 정규화.
     */
    private String readCellAsString(Cell cell) {
        if (cell == null) return null;
        String value = switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> readNumericCell(cell);
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> FORMATTER.formatCellValue(cell);
            default -> "";
        };
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String readNumericCell(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().format(ISO_DATE);
        }
        double d = cell.getNumericCellValue();
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return Long.toString((long) d);
        }
        return Double.toString(d);
    }

    /**
     * 도메인 column setter 가 자주 쓰는 헬퍼 — yyyy-MM-dd / yyyy/MM/dd / yyyyMMdd 모두 허용.
     */
    public static LocalDate parseDate(String field, String value) {
        if (value == null) return null;
        String normalized = value.replace('/', '-').replace('.', '-');
        if (normalized.length() == 8 && normalized.chars().allMatch(Character::isDigit)) {
            normalized = normalized.substring(0, 4) + "-" + normalized.substring(4, 6) + "-" + normalized.substring(6);
        }
        try {
            return LocalDate.parse(normalized, ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new RowParseException(field, "날짜 형식이 올바르지 않습니다 (예: 2025-01-31)");
        }
    }

    public record ParsedRow<B>(int rowNum, B builder) {
    }

    public record ParsedRows<B>(int totalRows, List<ParsedRow<B>> builders, List<ExcelRowError> errors) {

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}
