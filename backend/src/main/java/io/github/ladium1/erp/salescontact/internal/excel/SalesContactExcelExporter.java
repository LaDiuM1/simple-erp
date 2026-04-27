package io.github.ladium1.erp.salescontact.internal.excel;

import io.github.ladium1.erp.global.excel.ExcelColumn;
import io.github.ladium1.erp.global.excel.ExcelExporter;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactExcelRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SalesContactExcelExporter {

    private static final String SHEET_NAME = "영업 명부";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final List<ExcelColumn<SalesContactExcelRow>> COLUMNS = List.of(
            new ExcelColumn<>("이름", SalesContactExcelRow::name),
            new ExcelColumn<>("영문명", SalesContactExcelRow::nameEn),
            new ExcelColumn<>("휴대폰", SalesContactExcelRow::mobilePhone),
            new ExcelColumn<>("전화번호", SalesContactExcelRow::officePhone),
            new ExcelColumn<>("회사 이메일", SalesContactExcelRow::email),
            new ExcelColumn<>("개인 이메일", SalesContactExcelRow::personalEmail),
            new ExcelColumn<>("최초 미팅일", r -> r.metAt() == null ? null : r.metAt().format(DATE_FORMAT)),
            new ExcelColumn<>("만난 경로", SalesContactExcelRow::sources),
            new ExcelColumn<>("현재 회사", SalesContactExcelRow::currentCompanyName),
            new ExcelColumn<>("현재 직책", SalesContactExcelRow::currentPosition),
            new ExcelColumn<>("현재 부서", SalesContactExcelRow::currentDepartment),
            new ExcelColumn<>("비고", SalesContactExcelRow::note)
    );

    private final ExcelExporter excelExporter;

    public byte[] export(List<SalesContactExcelRow> rows) {
        return excelExporter.export(SHEET_NAME, COLUMNS, rows);
    }
}
