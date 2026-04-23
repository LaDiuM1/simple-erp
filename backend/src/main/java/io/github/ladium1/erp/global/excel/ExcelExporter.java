package io.github.ladium1.erp.global.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 컬럼 정의와 데이터 행을 받아 .xlsx 바이트 배열을 생성한다.
 * 헤더는 굵은 글씨 + 회색 배경, 컬럼 폭은 콘텐츠 자동 + 약간의 여백.
 */
@Component
public class ExcelExporter {

    private static final int COLUMN_PADDING = 512;
    private static final int COLUMN_MAX_WIDTH = 20_000;

    public <T> byte[] export(String sheetName, List<ExcelColumn<T>> columns, List<T> rows) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = headerStyle(workbook);

            writeHeader(sheet, columns, headerStyle);
            writeRows(sheet, columns, rows);
            sizeColumns(sheet, columns.size());

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("엑셀 파일 생성 실패", e);
        }
    }

    private <T> void writeHeader(Sheet sheet, List<ExcelColumn<T>> columns, CellStyle style) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns.get(i).header());
            cell.setCellStyle(style);
        }
    }

    private <T> void writeRows(Sheet sheet, List<ExcelColumn<T>> columns, List<T> rows) {
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            T data = rows.get(rowIndex);
            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                Cell cell = row.createCell(colIndex);
                String value = columns.get(colIndex).extract(data);
                cell.setCellValue(value == null ? "" : value);
            }
        }
    }

    private void sizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            int width = Math.min(sheet.getColumnWidth(i) + COLUMN_PADDING, COLUMN_MAX_WIDTH);
            sheet.setColumnWidth(i, width);
        }
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
