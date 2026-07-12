package io.github.ladium1.erp.equipment.internal.excel;

import io.github.ladium1.erp.equipment.internal.dto.EquipmentExcelRow;
import io.github.ladium1.erp.global.excel.ExcelColumn;
import io.github.ladium1.erp.global.excel.ExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EquipmentExcelExporter {

    private static final String SHEET_NAME = "설비대장";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final List<ExcelColumn<EquipmentExcelRow>> COLUMNS = List.of(
            new ExcelColumn<>("고객사", EquipmentExcelRow::customerName),
            new ExcelColumn<>("설치 주소", EquipmentExcelRow::installAddress),
            new ExcelColumn<>("공급사", EquipmentExcelRow::supplierName),
            new ExcelColumn<>("유형", EquipmentExcelRow::categoryName),
            new ExcelColumn<>("모델명", EquipmentExcelRow::productModelName),
            new ExcelColumn<>("출력", r -> r.outputValue() == null
                    ? null
                    : r.outputValue().stripTrailingZeros().toPlainString()
                            + (r.outputUnit() == null ? "" : r.outputUnit().getDescription())),
            new ExcelColumn<>("시리얼", EquipmentExcelRow::serialNo),
            new ExcelColumn<>("설치일", r -> formatDate(r.installedDate())),
            new ExcelColumn<>("설치완료확인서 일자", r -> formatDate(r.confirmedDate())),
            new ExcelColumn<>("보증 기산일", r -> formatDate(r.warrantyStartDate())),
            new ExcelColumn<>("발진기 보증 (개월)", r -> formatMonths(r.oscillatorWarrantyMonths())),
            new ExcelColumn<>("발진기 보증 만료일", r -> formatDate(r.oscillatorWarrantyEndDate())),
            new ExcelColumn<>("무상 AS (개월)", r -> formatMonths(r.generalWarrantyMonths())),
            new ExcelColumn<>("무상 AS 만료일", r -> formatDate(r.generalWarrantyEndDate())),
            new ExcelColumn<>("보증보험", r -> r.warrantyInsurance() ? "가입" : "미가입"),
            new ExcelColumn<>("계약번호", EquipmentExcelRow::contractNo),
            new ExcelColumn<>("비고", EquipmentExcelRow::note)
    );

    private final ExcelExporter excelExporter;

    public byte[] export(List<EquipmentExcelRow> rows) {
        return excelExporter.export(SHEET_NAME, COLUMNS, rows);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMAT);
    }

    private static String formatMonths(Integer months) {
        return months == null ? null : String.valueOf(months);
    }
}
