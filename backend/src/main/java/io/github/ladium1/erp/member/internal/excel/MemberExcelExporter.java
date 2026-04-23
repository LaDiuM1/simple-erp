package io.github.ladium1.erp.member.internal.excel;

import io.github.ladium1.erp.global.excel.ExcelColumn;
import io.github.ladium1.erp.global.excel.ExcelExporter;
import io.github.ladium1.erp.member.internal.dto.MemberSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberExcelExporter {

    private static final String SHEET_NAME = "직원";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final List<ExcelColumn<MemberSummaryResponse>> COLUMNS = List.of(
            new ExcelColumn<>("로그인 ID", MemberSummaryResponse::loginId),
            new ExcelColumn<>("이름", MemberSummaryResponse::name),
            new ExcelColumn<>("이메일", MemberSummaryResponse::email),
            new ExcelColumn<>("연락처", MemberSummaryResponse::phone),
            new ExcelColumn<>("부서", MemberSummaryResponse::departmentName),
            new ExcelColumn<>("직책", MemberSummaryResponse::positionName),
            new ExcelColumn<>("권한", MemberSummaryResponse::roleName),
            new ExcelColumn<>("입사일", m -> m.joinDate() == null ? null : m.joinDate().format(DATE_FORMAT)),
            new ExcelColumn<>("상태", m -> m.status() == null ? null : m.status().getDescription())
    );

    private final ExcelExporter excelExporter;

    public byte[] export(List<MemberSummaryResponse> members) {
        return excelExporter.export(SHEET_NAME, COLUMNS, members);
    }
}
