package io.github.ladium1.erp.afterservice.internal.excel;

import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceExcelRow;
import io.github.ladium1.erp.global.excel.ExcelColumn;
import io.github.ladium1.erp.global.excel.ExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AfterServiceExcelExporter {

    private static final String SHEET_NAME = "AS현황";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final List<ExcelColumn<AfterServiceExcelRow>> COLUMNS = List.of(
            new ExcelColumn<>("접수번호", AfterServiceExcelRow::receiptNo),
            new ExcelColumn<>("고객사", AfterServiceExcelRow::customerName),
            new ExcelColumn<>("설비명", AfterServiceExcelRow::equipmentModelName),
            new ExcelColumn<>("시리얼", AfterServiceExcelRow::equipmentSerialNo),
            new ExcelColumn<>("유형", r -> r.type() == null ? null : r.type().getDescription()),
            new ExcelColumn<>("상태", r -> r.status() == null ? null : r.status().getDescription()),
            new ExcelColumn<>("접수일", r -> formatDate(r.receivedDate())),
            new ExcelColumn<>("완료일", r -> formatDate(r.completedDate())),
            new ExcelColumn<>("주 담당", AfterServiceExcelRow::assignedEngineerName),
            new ExcelColumn<>("유상/무상", r -> r.warrantyDecision() == null ? null : r.warrantyDecision().getDescription()),
            new ExcelColumn<>("청구액", r -> formatAmount(r.billingAmount())),
            new ExcelColumn<>("경비 합계", r -> formatAmount(r.expenseTotal())),
            new ExcelColumn<>("증상", AfterServiceExcelRow::symptom)
    );

    private final ExcelExporter excelExporter;

    public byte[] export(List<AfterServiceExcelRow> rows) {
        return excelExporter.export(SHEET_NAME, COLUMNS, rows);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMAT);
    }

    private static String formatAmount(Long amount) {
        return amount == null ? null : String.format("%,d", amount);
    }
}
