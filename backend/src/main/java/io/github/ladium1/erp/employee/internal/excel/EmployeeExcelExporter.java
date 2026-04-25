package io.github.ladium1.erp.employee.internal.excel;

import io.github.ladium1.erp.global.excel.ExcelColumn;
import io.github.ladium1.erp.global.excel.ExcelExporter;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EmployeeExcelExporter {

    private static final String SHEET_NAME = "직원";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final List<ExcelColumn<EmployeeSummaryResponse>> COLUMNS = List.of(
            new ExcelColumn<>("로그인 ID", EmployeeSummaryResponse::loginId),
            new ExcelColumn<>("이름", EmployeeSummaryResponse::name),
            new ExcelColumn<>("이메일", EmployeeSummaryResponse::email),
            new ExcelColumn<>("연락처", EmployeeSummaryResponse::phone),
            new ExcelColumn<>("부서", EmployeeSummaryResponse::departmentName),
            new ExcelColumn<>("직책", EmployeeSummaryResponse::positionName),
            new ExcelColumn<>("권한", EmployeeSummaryResponse::roleName),
            new ExcelColumn<>("입사일", m -> m.joinDate() == null ? null : m.joinDate().format(DATE_FORMAT)),
            new ExcelColumn<>("상태", m -> m.status() == null ? null : m.status().getDescription())
    );

    private final ExcelExporter excelExporter;

    public byte[] export(List<EmployeeSummaryResponse> employees) {
        return excelExporter.export(SHEET_NAME, COLUMNS, employees);
    }
}
