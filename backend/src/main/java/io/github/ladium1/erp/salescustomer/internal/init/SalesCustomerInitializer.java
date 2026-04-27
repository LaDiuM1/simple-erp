package io.github.ladium1.erp.salescustomer.internal.init;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.salescontact.api.SalesContactApi;
import io.github.ladium1.erp.salescontact.api.dto.RecentSalesContactInfo;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivity;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivityType;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesAssignment;
import io.github.ladium1.erp.salescustomer.internal.repository.SalesActivityRepository;
import io.github.ladium1.erp.salescustomer.internal.repository.SalesAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 서버 실행 시 고객사 영업 (배정 + 활동) 테스트 데이터 시드 — 배정이 하나도 없을 때만.
 * <p>
 * customer / employee / salescontact 의 시드 데이터가 먼저 존재해야 함.
 * 의존 데이터가 없으면 경고 로그만 남기고 스킵 (재기동 시 다시 시도).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesCustomerInitializer implements ApplicationRunner {

    private final SalesAssignmentRepository assignmentRepository;
    private final SalesActivityRepository activityRepository;
    private final CustomerApi customerApi;
    private final EmployeeApi employeeApi;
    private final SalesContactApi salesContactApi;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (assignmentRepository.count() > 0) {
            return;
        }

        Map<String, Long> customerByCode = customerApi.findAll().stream()
                .collect(Collectors.toMap(CustomerInfo::code, CustomerInfo::id));
        if (customerByCode.isEmpty()) {
            log.warn("고객사 시드가 없어 고객사 영업 테스트 데이터 초기화를 건너뜀 — 재기동 시 다시 시도");
            return;
        }

        Optional<Long> user01 = employeeApi.findByLoginId("user01").map(EmployeeInfo::id);
        Optional<Long> user02 = employeeApi.findByLoginId("user02").map(EmployeeInfo::id);
        Optional<Long> user03 = employeeApi.findByLoginId("user03").map(EmployeeInfo::id);
        if (user01.isEmpty() || user02.isEmpty() || user03.isEmpty()) {
            log.warn("직원 시드가 없어 고객사 영업 테스트 데이터 초기화를 건너뜀 — 재기동 시 다시 시도");
            return;
        }

        Map<String, Long> contactByName = salesContactApi.findRecent(20).stream()
                .collect(Collectors.toMap(RecentSalesContactInfo::name, RecentSalesContactInfo::id, (a, b) -> a));

        log.info("고객사 영업 테스트 데이터 초기화 시작");

        // 1. 배정 — 한 고객사에 여러 직원 / 종료 이력 / 주담당 변경 케이스 혼합
        Long c1 = customerByCode.get("C0001");
        Long c2 = customerByCode.get("C0002");
        Long c3 = customerByCode.get("C0003");
        Long c4 = customerByCode.get("C0004");
        Long c5 = customerByCode.get("C0005");

        if (c1 != null) {
            saveAssignment(c1, user01.get(), LocalDate.of(2025, 9, 1), null, true, "신규 고객사 주담당");
            saveAssignment(c1, user02.get(), LocalDate.of(2025, 12, 1), null, false, "공동 담당 추가");
        }
        if (c2 != null) {
            saveAssignment(c2, user02.get(), LocalDate.of(2025, 10, 15), null, true, "신규 고객사 주담당");
        }
        if (c3 != null) {
            saveAssignment(c3, user03.get(), LocalDate.of(2026, 1, 10), null, true, "파트너사 신규 등록");
        }
        if (c4 != null) {
            saveAssignment(c4, user01.get(), LocalDate.of(2026, 3, 20), null, true, "신규 고객사 주담당");
        }
        if (c5 != null) {
            saveAssignment(c5, user03.get(),
                    LocalDate.of(2024, 5, 1), LocalDate.of(2025, 8, 31), false,
                    "고객사 휴면 처리로 배정 종료");
        }

        // 2. 활동 — 진행 중 고객사 위주, 담당자 ID 매칭 + 자유 입력 fallback 혼합
        if (c1 != null) {
            saveActivity(c1, SalesActivityType.VISIT,
                    LocalDateTime.of(2026, 4, 15, 10, 0),
                    "첫 방문 미팅", "사무실 방문하여 회사 소개 및 제품 데모 진행",
                    user01.get(), contactByName.get("정대성"), "정대성", "구매팀장");
            saveActivity(c1, SalesActivityType.EMAIL,
                    LocalDateTime.of(2026, 4, 20, 14, 30),
                    "견적서 발송", "표준 라인업 견적 + 옵션 견적 송부",
                    user02.get(), null, "정대성", "구매팀장");
        }
        if (c2 != null) {
            saveActivity(c2, SalesActivityType.CALL,
                    LocalDateTime.of(2026, 4, 10, 11, 15),
                    "신규 프로젝트 문의 응대", "내부 검토 후 회신 예정",
                    user02.get(), contactByName.get("이한빛"), "이한빛", null);
            saveActivity(c2, SalesActivityType.MEETING,
                    LocalDateTime.of(2026, 4, 22, 15, 0),
                    "요구사항 정리 미팅", "프로젝트 범위 / 일정 / 예산 1차 합의",
                    user02.get(), null, "이한빛", "기획팀장");
        }
        if (c3 != null) {
            saveActivity(c3, SalesActivityType.VISIT,
                    LocalDateTime.of(2026, 3, 25, 13, 30),
                    "파트너십 킥오프", "공동 사업 모델 논의",
                    user03.get(), contactByName.get("박파트너"), "박파트너", "대표");
            saveActivity(c3, SalesActivityType.EMAIL,
                    LocalDateTime.of(2026, 4, 18, 9, 0),
                    "계약서 초안 송부", "법무 검토 요청",
                    user03.get(), null, "박파트너", "대표");
        }
        if (c4 != null) {
            saveActivity(c4, SalesActivityType.CALL,
                    LocalDateTime.of(2026, 4, 5, 16, 45),
                    "초기 컨택", "관심 분야 청취 및 후속 미팅 일정 조율",
                    user01.get(), contactByName.get("최미래"), "최미래", null);
        }
        if (c5 != null) {
            saveActivity(c5, SalesActivityType.OTHER,
                    LocalDateTime.of(2025, 7, 10, 10, 0),
                    "휴면 전환 안내",
                    "내부 정책 변경에 따른 거래 일시 중단 안내. 재개 시 별도 컨택 예정",
                    user03.get(), null, "강휴면", null);
        }

        log.info("고객사 영업 테스트 데이터 초기화 완료 — 배정 {}건 / 활동 {}건",
                assignmentRepository.count(), activityRepository.count());
    }

    private void saveAssignment(Long customerId,
                                Long employeeId,
                                LocalDate startDate,
                                LocalDate endDate,
                                boolean primary,
                                String reason) {
        assignmentRepository.save(SalesAssignment.builder()
                .customerId(customerId)
                .employeeId(employeeId)
                .startDate(startDate)
                .endDate(endDate)
                .primary(primary)
                .reason(reason)
                .build());
    }

    @SuppressWarnings("SameParameterValue")
    private void saveActivity(Long customerId,
                              SalesActivityType type,
                              LocalDateTime activityDate,
                              String subject,
                              String content,
                              Long ourEmployeeId,
                              Long customerContactId,
                              String customerContactName,
                              String customerContactPosition) {
        activityRepository.save(SalesActivity.builder()
                .customerId(customerId)
                .type(type)
                .activityDate(activityDate)
                .subject(subject)
                .content(content)
                .ourEmployeeId(ourEmployeeId)
                .customerContactId(customerContactId)
                .customerContactName(customerContactName)
                .customerContactPosition(customerContactPosition)
                .build());
    }
}
