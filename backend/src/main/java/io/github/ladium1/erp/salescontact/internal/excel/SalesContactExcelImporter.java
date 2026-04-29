package io.github.ladium1.erp.salescontact.internal.excel;

import io.github.ladium1.erp.global.excel.ExcelExporter;
import io.github.ladium1.erp.global.excel.ExcelImportColumn;
import io.github.ladium1.erp.global.excel.ExcelImporter;
import io.github.ladium1.erp.global.excel.ExcelImporter.ParsedRows;
import io.github.ladium1.erp.global.excel.RowParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 영업 명부 엑셀 업로드 파서 — 다운로드와 동일한 컬럼 헤더.
 * <p>
 * 만난 경로는 콤마로 구분된 이름 리스트 (다운로드 결과와 대응). 이름은 acquisition_sources.name 으로 lookup.
 * 현재 회사 / 직책 / 부서가 채워지면 활성 재직 이력 (외부 회사 자유 입력) 으로 함께 생성.
 */
@Component
@RequiredArgsConstructor
public class SalesContactExcelImporter {

    private static final String SHEET_NAME = "영업 명부";

    private final ExcelImporter excelImporter;
    private final ExcelExporter excelExporter;

    public ParsedRows<Holder> parse(MultipartFile file) {
        return excelImporter.parse(file, COLUMNS, Holder::new);
    }

    public byte[] exportTemplate() {
        return excelExporter.exportImportTemplate(SHEET_NAME, COLUMNS);
    }

    public static final class Holder {
        public String name;
        public String nameEn;
        public String mobilePhone;
        public String officePhone;
        public String email;
        public String personalEmail;
        public LocalDate metAt;
        /** 콤마로 구분된 컨택 경로 이름들 — 검증/매핑은 service 단계에서 진행. */
        public List<String> sourceNames = List.of();
        public String currentCompanyName;
        public String currentPosition;
        public String currentDepartment;
        public String note;
    }

    private static final List<ExcelImportColumn<Holder>> COLUMNS = List.of(
            ExcelImportColumn.required(
                    "이름",
                    "텍스트 (최대 50자)",
                    "홍길동",
                    (h, v) -> {
                        if (v == null) throw new RowParseException("이름", "이름을 입력해주세요.");
                        h.name = v;
                    }),
            ExcelImportColumn.optional(
                    "영문명",
                    "텍스트 (최대 50자)",
                    "Gildong Hong",
                    (h, v) -> h.nameEn = v),
            ExcelImportColumn.optional(
                    "휴대폰",
                    "전화번호 텍스트 — 중복 불가",
                    "010-1234-5678",
                    (h, v) -> h.mobilePhone = v),
            ExcelImportColumn.optional(
                    "전화번호",
                    "사무실 전화번호 텍스트",
                    "02-1234-5678",
                    (h, v) -> h.officePhone = v),
            ExcelImportColumn.optional(
                    "회사 이메일",
                    "이메일 형식",
                    "gildong@hong.com",
                    (h, v) -> h.email = v),
            ExcelImportColumn.optional(
                    "개인 이메일",
                    "이메일 형식",
                    "gildong@gmail.com",
                    (h, v) -> h.personalEmail = v),
            ExcelImportColumn.optional(
                    "최초 미팅일",
                    "YYYY-MM-DD (날짜)",
                    "2025-03-10",
                    (h, v) -> h.metAt = ExcelImporter.parseDate("최초 미팅일", v)),
            ExcelImportColumn.optional(
                    "만난 경로",
                    "사전 등록된 경로명만 입력 가능 (여러 항목 입력 시 콤마로 구분)",
                    "전시회 2024, 김OO 소개",
                    (h, v) -> h.sourceNames = splitNames(v)),
            ExcelImportColumn.optional(
                    "현재 회사",
                    "입력 시 해당 회사의 '현재 재직 중' 이력 자동 생성",
                    "(주)홍길동",
                    (h, v) -> h.currentCompanyName = v),
            ExcelImportColumn.optional(
                    "현재 직책",
                    "'현재 회사'가 입력된 경우에만 저장",
                    "팀장",
                    (h, v) -> h.currentPosition = v),
            ExcelImportColumn.optional(
                    "현재 부서",
                    "'현재 회사'가 입력된 경우에만 저장",
                    "영업팀",
                    (h, v) -> h.currentDepartment = v),
            ExcelImportColumn.optional(
                    "비고",
                    "자유 텍스트",
                    "주요 고객사 미팅 후 명함 교환",
                    (h, v) -> h.note = v)
    );

    private static List<String> splitNames(String value) {
        if (value == null) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
