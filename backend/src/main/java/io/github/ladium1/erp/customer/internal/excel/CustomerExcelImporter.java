package io.github.ladium1.erp.customer.internal.excel;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.customer.internal.dto.CustomerCreateRequest;
import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import io.github.ladium1.erp.global.excel.ExcelExporter;
import io.github.ladium1.erp.global.excel.ExcelImportColumn;
import io.github.ladium1.erp.global.excel.ExcelImporter;
import io.github.ladium1.erp.global.excel.ExcelImporter.ParsedRows;
import io.github.ladium1.erp.global.excel.RowParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 고객사 엑셀 업로드 파서 — 다운로드와 동일한 컬럼 헤더를 사용해 사용자가 다운로드한 파일을 그대로 수정 후 재업로드 가능.
 * <p>
 * 코드 컬럼은 채번 규칙이 결정 — MANUAL 이면 템플릿 헤더에 `*` (필수), AUTO / AUTO_OR_MANUAL 이면 비워두면 자동 생성.
 * 분류 / 상태 컬럼은 한국어 description 또는 enum name 양쪽 입력 허용.
 */
@Component
@RequiredArgsConstructor
public class CustomerExcelImporter {

    private static final String SHEET_NAME = "고객사";

    private final ExcelImporter excelImporter;
    private final ExcelExporter excelExporter;
    private final CodeRuleApi codeRuleApi;

    public ParsedRows<Holder> parse(MultipartFile file) {
        // 파싱 단계는 required 메타가 영향 없음 — 임의 값으로 빌드.
        return excelImporter.parse(file, columns(false), Holder::new);
    }

    /**
     * 채번 규칙의 inputMode 가 MANUAL 일 때만 코드 컬럼 헤더에 `*` 마킹 + 필드 안내 시트에 "필수".
     */
    public byte[] exportTemplate() {
        InputMode mode = codeRuleApi.getRule(CodeRuleTarget.CUSTOMER).inputMode();
        boolean codeRequired = mode == InputMode.MANUAL;
        return excelExporter.exportImportTemplate(SHEET_NAME, columns(codeRequired));
    }

    /**
     * 파싱 결과를 담는 holder — record 가 아닌 mutable 클래스라 컬럼별 setter 가 단계적으로 채울 수 있음.
     */
    public static final class Holder {
        public String code;
        public String name;
        public String bizRegNo;
        public String representative;
        public String phone;
        public String email;
        public String roadAddress;
        public CustomerType type;
        public CustomerStatus status;
        public java.time.LocalDate tradeStartDate;

        public CustomerCreateRequest toRequest() {
            return new CustomerCreateRequest(
                    code, name, null, bizRegNo, null, representative, null, null,
                    phone, null, email, null,
                    null, roadAddress, null,
                    type, status, tradeStartDate, null
            );
        }
    }

    private static List<ExcelImportColumn<Holder>> columns(boolean codeRequired) {
        ExcelImportColumn<Holder> codeColumn = codeRequired
                ? ExcelImportColumn.required(
                        "고객사 코드",
                        "텍스트 — 채번 규칙 패턴 준수 (수동 입력 모드)",
                        "ABC-001",
                        (h, v) -> h.code = v)
                : ExcelImportColumn.optional(
                        "고객사 코드",
                        "텍스트 — 비우면 채번 규칙에 따라 자동 생성",
                        "(빈 칸 또는 ABC-001)",
                        (h, v) -> h.code = v);

        return List.of(
                codeColumn,
                ExcelImportColumn.required(
                        "고객사명",
                        "텍스트 (최대 200자)",
                        "(주)홍길동",
                        (h, v) -> {
                            if (v == null) throw new RowParseException("고객사명", "고객사명을 입력해주세요.");
                            h.name = v;
                        }),
                ExcelImportColumn.optional(
                        "사업자등록번호",
                        "10자리 숫자 (하이픈 포함) — 중복 불가",
                        "123-45-67890",
                        (h, v) -> h.bizRegNo = v),
                ExcelImportColumn.optional(
                        "대표자",
                        "텍스트 (최대 50자)",
                        "홍길동",
                        (h, v) -> h.representative = v),
                ExcelImportColumn.optional(
                        "전화",
                        "전화번호 텍스트",
                        "02-1234-5678",
                        (h, v) -> h.phone = v),
                ExcelImportColumn.optional(
                        "이메일",
                        "이메일 형식",
                        "contact@hong.com",
                        (h, v) -> h.email = v),
                ExcelImportColumn.optional(
                        "주소",
                        "도로명 주소 텍스트",
                        "서울시 강남구 테헤란로 1",
                        (h, v) -> h.roadAddress = v),
                ExcelImportColumn.requiredEnum(
                        "분류",
                        "고정 값 중 택 1",
                        "잠재고객, 일반고객, 주요고객, 파트너",
                        "일반고객",
                        (h, v) -> h.type = parseType(v)),
                ExcelImportColumn.requiredEnum(
                        "상태",
                        "고정 값 중 택 1",
                        "거래중, 비거래, 거래중지",
                        "거래중",
                        (h, v) -> h.status = parseStatus(v)),
                ExcelImportColumn.optional(
                        "거래시작일",
                        "YYYY-MM-DD (날짜)",
                        "2025-01-15",
                        (h, v) -> h.tradeStartDate = ExcelImporter.parseDate("거래시작일", v))
        );
    }

    private static CustomerType parseType(String value) {
        if (value == null) {
            throw new RowParseException("분류", "분류를 입력해주세요.");
        }
        for (CustomerType t : CustomerType.values()) {
            if (t.name().equalsIgnoreCase(value) || t.getDescription().equals(value)) {
                return t;
            }
        }
        throw new RowParseException("분류", "분류는 잠재고객 / 일반고객 / 주요고객 / 파트너 중 하나여야 합니다.");
    }

    private static CustomerStatus parseStatus(String value) {
        if (value == null) {
            throw new RowParseException("상태", "상태를 입력해주세요.");
        }
        for (CustomerStatus s : CustomerStatus.values()) {
            if (s.name().equalsIgnoreCase(value) || s.getDescription().equals(value)) {
                return s;
            }
        }
        throw new RowParseException("상태", "상태는 거래중 / 비거래 / 거래중지 중 하나여야 합니다.");
    }
}
