package io.github.ladium1.erp.global.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 컬럼 정의와 데이터 행을 받아 .xlsx 바이트 배열을 생성한다.
 * 헤더는 굵은 글씨 + 회색 배경, 모든 셀은 가운데 정렬 + thin border,
 * 컬럼 폭은 헤더 텍스트 길이 기반으로 일정한 패딩을 더해 산정 (다운로드 / 업로드 템플릿 톤 동일).
 */
@Component
public class ExcelExporter {

    /** POI 컬럼 폭 단위는 1/256 character. 헤더 글자당 폭 + 좌우 패딩. */
    private static final int CHAR_WIDTH = 256;
    private static final int COLUMN_PADDING_CHARS = 6;
    private static final int COLUMN_MIN_CHARS = 10;
    private static final int COLUMN_MAX_CHARS = 60;

    public <T> byte[] export(String sheetName, List<ExcelColumn<T>> columns, List<T> rows) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle bodyStyle = bodyStyle(workbook);

            writeHeader(sheet, columns, headerStyle);
            writeRows(sheet, columns, rows, bodyStyle);
            sizeColumns(sheet, columns);

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("엑셀 파일 생성 실패", e);
        }
    }

    /**
     * 데이터 없이 헤더 + 1행 가이드 (예시 또는 빈 행) 만 포함된 빈 워크북 — 업로드 템플릿용.
     */
    public <T> byte[] exportTemplate(String sheetName, List<ExcelColumn<T>> columns) {
        return export(sheetName, columns, List.of());
    }

    /**
     * 업로드 템플릿 생성 — 세 시트 구조.
     * <ul>
     *     <li>시트 1 ({@code primarySheetName}): 항목별 {@link ExcelImportColumn#displayHeader()} (required 면 끝에 `*`) 열 제목 + 1행 빈 본문.</li>
     *     <li>시트 2 "항목 가로 안내": 항목당 한 행 — 5 컬럼 (항목명 / 필수 / 입력 형식 및 제약 / 허용 값 / 예시).</li>
     *     <li>시트 3 "항목 세로 안내": 항목당 한 컬럼 — 4 행 속성 라벨 (필수 / 입력 형식 및 제약 / 허용 값 / 예시) + 첫 행은 항목명 헤더.</li>
     * </ul>
     * 모든 시트가 {@link #export} 와 동일 톤 (가운데 정렬 + thin border + 자동 너비). 안내 시트 본문 셀은 wrap 적용.
     */
    public byte[] exportImportTemplate(String primarySheetName,
                                       List<? extends ExcelImportColumn<?>> columns) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            CellStyle headerStyle = headerStyle(workbook);
            CellStyle bodyStyle = bodyStyle(workbook);

            writePrimaryTemplateSheet(workbook, primarySheetName, columns, headerStyle, bodyStyle);
            writeFieldGuideHorizontalSheet(workbook, columns, headerStyle);
            writeFieldGuideVerticalSheet(workbook, columns, headerStyle);

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("엑셀 파일 생성 실패", e);
        }
    }

    private void writePrimaryTemplateSheet(Workbook workbook,
                                           String sheetName,
                                           List<? extends ExcelImportColumn<?>> columns,
                                           CellStyle headerStyle,
                                           CellStyle bodyStyle) {
        Sheet sheet = workbook.createSheet(sheetName);

        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns.get(i).displayHeader());
            cell.setCellStyle(headerStyle);
        }
        Row body = sheet.createRow(1);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = body.createCell(i);
            cell.setCellValue("");
            cell.setCellStyle(bodyStyle);
        }
        for (int i = 0; i < columns.size(); i++) {
            int chars = visualLength(columns.get(i).displayHeader()) + COLUMN_PADDING_CHARS;
            int clamped = Math.max(COLUMN_MIN_CHARS, Math.min(COLUMN_MAX_CHARS, chars));
            sheet.setColumnWidth(i, clamped * CHAR_WIDTH);
        }
    }

    /** 가로 안내 시트 — 5 컬럼 헤더 (항목당 한 행). */
    private static final String[] HORIZONTAL_GUIDE_HEADERS = {
            "항목명", "필수", "입력 형식 및 제약", "허용 값 (정확히 입력)", "예시"
    };

    /** 세로 안내 시트 — 4 속성 라벨 행 (항목당 한 컬럼). 항목명은 시트 첫 행에 자체 노출. */
    private static final String[] VERTICAL_GUIDE_ROW_LABELS = {
            "필수", "입력 형식 및 제약", "허용 값 (정확히 입력)", "예시"
    };

    /** 세로 안내 시트의 좌상단 코너 셀 라벨. */
    private static final String VERTICAL_GUIDE_CORNER_LABEL = "항목명";

    /** 자유 입력 항목의 "허용 값" 칸 — 시각적 빈칸보다 의미가 명확. */
    private static final String NO_ALLOWED_VALUES = "-";

    /**
     * "항목 가로 안내" — 항목당 한 행. 사용자가 위에서 아래로 한 항목씩 한눈에 읽기.
     */
    private void writeFieldGuideHorizontalSheet(Workbook workbook,
                                                List<? extends ExcelImportColumn<?>> columns,
                                                CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("항목 가로 안내");
        CellStyle wrapBodyStyle = wrapBodyStyle(workbook);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HORIZONTAL_GUIDE_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HORIZONTAL_GUIDE_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        for (int r = 0; r < columns.size(); r++) {
            String[] values = horizontalGuideRow(columns.get(r));
            Row row = sheet.createRow(r + 1);
            for (int c = 0; c < values.length; c++) {
                Cell cell = row.createCell(c);
                cell.setCellValue(values[c]);
                cell.setCellStyle(wrapBodyStyle);
            }
        }

        sizeHorizontalGuideColumns(sheet, columns);
    }

    private static String[] horizontalGuideRow(ExcelImportColumn<?> col) {
        return new String[] {
                col.header(),
                col.required() ? "필수" : "선택",
                col.description() == null ? "" : col.description(),
                allowedValuesOrDash(col),
                col.example() == null ? "" : col.example(),
        };
    }

    private void sizeHorizontalGuideColumns(Sheet sheet, List<? extends ExcelImportColumn<?>> columns) {
        int[] maxLens = new int[HORIZONTAL_GUIDE_HEADERS.length];
        for (int i = 0; i < HORIZONTAL_GUIDE_HEADERS.length; i++) {
            maxLens[i] = visualLength(HORIZONTAL_GUIDE_HEADERS[i]);
        }
        for (ExcelImportColumn<?> col : columns) {
            String[] values = horizontalGuideRow(col);
            for (int i = 0; i < values.length; i++) {
                maxLens[i] = Math.max(maxLens[i], visualLength(values[i]));
            }
        }
        for (int i = 0; i < HORIZONTAL_GUIDE_HEADERS.length; i++) {
            sheet.setColumnWidth(i, clampWidth(maxLens[i]) * CHAR_WIDTH);
        }
    }

    /**
     * "항목 세로 안내" — 항목당 한 컬럼. 4 속성 라벨이 행으로 고정, 좌측에서 우측으로 항목별 비교에 유리.
     */
    private void writeFieldGuideVerticalSheet(Workbook workbook,
                                              List<? extends ExcelImportColumn<?>> columns,
                                              CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("항목 세로 안내");
        CellStyle wrapBodyStyle = wrapBodyStyle(workbook);

        Row headerRow = sheet.createRow(0);
        Cell corner = headerRow.createCell(0);
        corner.setCellValue(VERTICAL_GUIDE_CORNER_LABEL);
        corner.setCellStyle(headerStyle);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i + 1);
            cell.setCellValue(columns.get(i).header());
            cell.setCellStyle(headerStyle);
        }

        for (int r = 0; r < VERTICAL_GUIDE_ROW_LABELS.length; r++) {
            Row row = sheet.createRow(r + 1);
            Cell label = row.createCell(0);
            label.setCellValue(VERTICAL_GUIDE_ROW_LABELS[r]);
            label.setCellStyle(headerStyle);
            for (int c = 0; c < columns.size(); c++) {
                Cell cell = row.createCell(c + 1);
                cell.setCellValue(verticalGuideValue(columns.get(c), r));
                cell.setCellStyle(wrapBodyStyle);
            }
        }

        sizeVerticalGuideColumns(sheet, columns);
    }

    private static String verticalGuideValue(ExcelImportColumn<?> col, int rowIndex) {
        return switch (rowIndex) {
            case 0 -> col.required() ? "필수" : "선택";
            case 1 -> col.description() == null ? "" : col.description();
            case 2 -> allowedValuesOrDash(col);
            case 3 -> col.example() == null ? "" : col.example();
            default -> "";
        };
    }

    private void sizeVerticalGuideColumns(Sheet sheet, List<? extends ExcelImportColumn<?>> columns) {
        int firstColLen = visualLength(VERTICAL_GUIDE_CORNER_LABEL);
        for (String label : VERTICAL_GUIDE_ROW_LABELS) {
            firstColLen = Math.max(firstColLen, visualLength(label));
        }
        sheet.setColumnWidth(0, clampWidth(firstColLen) * CHAR_WIDTH);

        for (int i = 0; i < columns.size(); i++) {
            ExcelImportColumn<?> col = columns.get(i);
            int maxLen = visualLength(col.header());
            for (int r = 0; r < VERTICAL_GUIDE_ROW_LABELS.length; r++) {
                maxLen = Math.max(maxLen, visualLength(verticalGuideValue(col, r)));
            }
            sheet.setColumnWidth(i + 1, clampWidth(maxLen) * CHAR_WIDTH);
        }
    }

    private static String allowedValuesOrDash(ExcelImportColumn<?> col) {
        return (col.allowedValues() == null || col.allowedValues().isBlank())
                ? NO_ALLOWED_VALUES
                : col.allowedValues();
    }

    private static int clampWidth(int rawCharLength) {
        int chars = rawCharLength + COLUMN_PADDING_CHARS;
        return Math.max(COLUMN_MIN_CHARS, Math.min(COLUMN_MAX_CHARS, chars));
    }

    /** 안내 시트 본문 — 가운데 정렬 + thin border + 긴 값 wrap. 본 시트는 데이터 입력이 아닌 read-only 안내. */
    private CellStyle wrapBodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        applyAllBorders(style, BorderStyle.THIN);
        return style;
    }

    private <T> void writeHeader(Sheet sheet, List<ExcelColumn<T>> columns, CellStyle style) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns.get(i).header());
            cell.setCellStyle(style);
        }
    }

    private <T> void writeRows(Sheet sheet, List<ExcelColumn<T>> columns, List<T> rows, CellStyle bodyStyle) {
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            T data = rows.get(rowIndex);
            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                Cell cell = row.createCell(colIndex);
                String value = columns.get(colIndex).extract(data);
                cell.setCellValue(value == null ? "" : value);
                cell.setCellStyle(bodyStyle);
            }
        }
        // 빈 데이터일 때도 헤더 한 줄에 테두리가 자연스럽게 닫히도록 1행 빈 행을 같은 스타일로 추가.
        if (rows.isEmpty()) {
            Row row = sheet.createRow(1);
            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                Cell cell = row.createCell(colIndex);
                cell.setCellValue("");
                cell.setCellStyle(bodyStyle);
            }
        }
    }

    /**
     * 헤더 텍스트의 시각적 길이 (한글 1.7 / 영문 1.0 가중치) 에 패딩을 더해 폭 결정.
     * 데이터 길이는 무시 — 톤이 일정해야 다운로드 / 업로드 / 템플릿이 동일하게 보임.
     */
    private <T> void sizeColumns(Sheet sheet, List<ExcelColumn<T>> columns) {
        for (int i = 0; i < columns.size(); i++) {
            int chars = visualLength(columns.get(i).header()) + COLUMN_PADDING_CHARS;
            int clamped = Math.max(COLUMN_MIN_CHARS, Math.min(COLUMN_MAX_CHARS, chars));
            sheet.setColumnWidth(i, clamped * CHAR_WIDTH);
        }
    }

    private int visualLength(String text) {
        if (text == null || text.isEmpty()) return 0;
        double total = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // 한글 / CJK 영역은 한 글자가 라틴 문자 2자에 가까운 폭을 차지.
            total += (c > 0x7F) ? 1.7 : 1.0;
        }
        return (int) Math.ceil(total);
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        applyAllBorders(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle bodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyAllBorders(style, BorderStyle.THIN);
        return style;
    }

    private void applyAllBorders(CellStyle style, BorderStyle borderStyle) {
        style.setBorderTop(borderStyle);
        style.setBorderBottom(borderStyle);
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);
        short color = IndexedColors.GREY_50_PERCENT.getIndex();
        style.setTopBorderColor(color);
        style.setBottomBorderColor(color);
        style.setLeftBorderColor(color);
        style.setRightBorderColor(color);
    }

}
