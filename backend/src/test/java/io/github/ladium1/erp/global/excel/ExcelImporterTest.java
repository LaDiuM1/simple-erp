package io.github.ladium1.erp.global.excel;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelImporterTest {

    private final ExcelImporter importer = new ExcelImporter();
    private final List<ExcelImportColumn<Holder>> columns = List.of(
            ExcelImportColumn.required("이름", "텍스트", "홍길동", (holder, value) -> holder.name = value)
    );

    @Test
    @DisplayName("1MiB를 넘는 compressed Excel은 Workbook 생성 전에 거절")
    void rejects_compressed_file_larger_than_one_mebibyte() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[1024 * 1024 + 1]
        );

        assertThatThrownBy(() -> importer.parse(file, columns, Holder::new))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1MiB");
    }

    @Test
    @DisplayName("sparse lastRowNum으로 100행 순회 상한을 우회할 수 없음")
    void rejects_sparse_sheet_with_row_beyond_limit() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("sheet");
            sheet.createRow(0).createCell(0).setCellValue("이름");
            sheet.createRow(101).createCell(0).setCellValue("sparse");
            workbook.write(output);
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "sparse.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    output.toByteArray()
            );

            assertThatThrownBy(() -> importer.parse(file, columns, Holder::new))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("100행");
        }
    }

    @Test
    @DisplayName("정확히 100개 데이터 행은 허용")
    void accepts_exactly_one_hundred_rows() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("sheet");
            sheet.createRow(0).createCell(0).setCellValue("이름");
            for (int i = 1; i <= 100; i++) {
                sheet.createRow(i).createCell(0).setCellValue("row-" + i);
            }
            workbook.write(output);
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "boundary.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    output.toByteArray()
            );

            ExcelImporter.ParsedRows<Holder> parsed = importer.parse(file, columns, Holder::new);

            assertThat(parsed.totalRows()).isEqualTo(100);
            assertThat(parsed.builders()).hasSize(100);
        }
    }

    @Test
    @DisplayName("1MiB 안의 고압축 OOXML은 inflate 단계에서 거절하고 이후 정상 파일을 계속 처리")
    void rejects_high_compression_ooxml_and_remains_usable() throws Exception {
        byte[] compressedBomb;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("sheet");
            sheet.createRow(0).createCell(0).setCellValue("이름");
            String repeated = "A".repeat(32_000);
            for (int row = 1; row <= 5; row++) {
                sheet.createRow(row).createCell(0).setCellValue(row + repeated);
            }
            workbook.write(output);
            compressedBomb = output.toByteArray();
        }
        assertThat(compressedBomb.length).isLessThan(1024 * 1024);

        MockMultipartFile bomb = new MockMultipartFile(
                "file", "high-compression.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                compressedBomb
        );
        assertThatThrownBy(() -> importer.parse(bomb, columns, Holder::new))
                .isInstanceOfAny(IllegalArgumentException.class, RuntimeException.class);

        MockMultipartFile normal = workbookWithSingleName("정상 행");
        assertThat(importer.parse(normal, columns, Holder::new).totalRows()).isEqualTo(1);
    }

    private static MockMultipartFile workbookWithSingleName(String name) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("sheet");
            sheet.createRow(0).createCell(0).setCellValue("이름");
            sheet.createRow(1).createCell(0).setCellValue(name);
            workbook.write(output);
            return new MockMultipartFile(
                    "file", "normal.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    output.toByteArray()
            );
        }
    }

    private static final class Holder {
        private String name;
    }
}
