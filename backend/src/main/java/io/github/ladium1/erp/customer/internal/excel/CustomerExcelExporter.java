package io.github.ladium1.erp.customer.internal.excel;

import io.github.ladium1.erp.customer.internal.dto.CustomerSummaryResponse;
import io.github.ladium1.erp.global.excel.ExcelColumn;
import io.github.ladium1.erp.global.excel.ExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerExcelExporter {

    private static final String SHEET_NAME = "고객사";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final List<ExcelColumn<CustomerSummaryResponse>> COLUMNS = List.of(
            new ExcelColumn<>("고객사 코드", CustomerSummaryResponse::code),
            new ExcelColumn<>("고객사명", CustomerSummaryResponse::name),
            new ExcelColumn<>("사업자등록번호", CustomerSummaryResponse::bizRegNo),
            new ExcelColumn<>("대표자", CustomerSummaryResponse::representative),
            new ExcelColumn<>("전화", CustomerSummaryResponse::phone),
            new ExcelColumn<>("이메일", CustomerSummaryResponse::email),
            new ExcelColumn<>("주소", CustomerSummaryResponse::roadAddress),
            new ExcelColumn<>("분류", c -> c.type() == null ? null : c.type().getDescription()),
            new ExcelColumn<>("상태", c -> c.status() == null ? null : c.status().getDescription()),
            new ExcelColumn<>("거래시작일", c -> c.tradeStartDate() == null ? null : c.tradeStartDate().format(DATE_FORMAT))
    );

    private final ExcelExporter excelExporter;

    public byte[] export(List<CustomerSummaryResponse> customers) {
        return excelExporter.export(SHEET_NAME, COLUMNS, customers);
    }
}
