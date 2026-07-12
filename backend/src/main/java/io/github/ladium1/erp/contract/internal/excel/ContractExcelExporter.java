package io.github.ladium1.erp.contract.internal.excel;

import io.github.ladium1.erp.contract.internal.dto.ContractExcelRow;
import io.github.ladium1.erp.global.excel.ExcelColumn;
import io.github.ladium1.erp.global.excel.ExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ContractExcelExporter {

    private static final String SHEET_NAME = "설비계약";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final List<ExcelColumn<ContractExcelRow>> COLUMNS = List.of(
            new ExcelColumn<>("계약번호", ContractExcelRow::contractNo),
            new ExcelColumn<>("고객사", ContractExcelRow::customerName),
            new ExcelColumn<>("계약자", ContractExcelRow::employeeName),
            new ExcelColumn<>("공급사", ContractExcelRow::supplierName),
            new ExcelColumn<>("유형", ContractExcelRow::categoryName),
            new ExcelColumn<>("설비명", ContractExcelRow::productModelName),
            new ExcelColumn<>("출력", r -> r.outputValue() == null
                    ? null
                    : r.outputValue().stripTrailingZeros().toPlainString()
                            + (r.outputUnit() == null ? "" : r.outputUnit().getDescription())),
            new ExcelColumn<>("옵션", ContractExcelRow::optionText),
            new ExcelColumn<>("CRETOP", ContractExcelRow::cretopGrade),
            new ExcelColumn<>("지원사업", ContractExcelRow::supportProgramName),
            new ExcelColumn<>("지원 상태", r -> r.supportProgramStatus() == null ? null : r.supportProgramStatus().getDescription()),
            new ExcelColumn<>("상태", r -> r.status() == null ? null : r.status().getDescription()),
            new ExcelColumn<>("계약일", r -> formatDate(r.contractDate())),
            new ExcelColumn<>("납기일", r -> formatDate(r.dueDate())),
            new ExcelColumn<>("발주일", r -> formatDate(r.orderDate())),
            new ExcelColumn<>("입고 예정일", r -> formatDate(r.expectedArrivalDate())),
            new ExcelColumn<>("입고일", r -> formatDate(r.arrivalDate())),
            new ExcelColumn<>("설치 완료일", r -> formatDate(r.installedDate())),
            new ExcelColumn<>("정산 완료일", r -> formatDate(r.settledDate())),
            new ExcelColumn<>("초기 계약금액", r -> formatAmount(r.initialAmount())),
            new ExcelColumn<>("최종 계약금액", r -> formatAmount(r.finalAmount())),
            new ExcelColumn<>("입금 합계", r -> formatAmount(r.paidTotal())),
            new ExcelColumn<>("미수금", r -> formatAmount(r.outstandingAmount())),
            new ExcelColumn<>("물류 메모", ContractExcelRow::logisticsNote)
    );

    private final ExcelExporter excelExporter;

    public byte[] export(List<ContractExcelRow> rows) {
        return excelExporter.export(SHEET_NAME, COLUMNS, rows);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMAT);
    }

    private static String formatAmount(Long amount) {
        return amount == null ? null : String.format("%,d", amount);
    }
}
