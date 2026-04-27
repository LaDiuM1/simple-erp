package io.github.ladium1.erp.salescontact.internal.init;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.salescontact.internal.entity.DepartureType;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContactEmployment;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactEmploymentRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 서버 실행 시 영업 명부 + 재직 이력 테스트 데이터 시드 — 명부가 하나도 없을 때만.
 * 재직 이력은 customer 시드가 함께 존재할 때만 매핑되며, 없으면 명부만 생성한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesContactInitializer implements ApplicationRunner {

    private final SalesContactRepository contactRepository;
    private final SalesContactEmploymentRepository employmentRepository;
    private final CustomerApi customerApi;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (contactRepository.count() > 0) {
            return;
        }

        Map<String, Long> customerByCode = customerApi.findAll().stream()
                .collect(Collectors.toMap(CustomerInfo::code, CustomerInfo::id));
        if (customerByCode.isEmpty()) {
            log.warn("고객사 시드가 없어 영업 명부 테스트 데이터 초기화를 건너뜀 — 재기동 시 다시 시도");
            return;
        }

        log.info("영업 명부 테스트 데이터 초기화 시작");

        SalesContact daesung = save("정대성", "Daesung Jung", "010-1111-2222", "02-3000-1000",
                "ds.jung@daesung.co.kr", null,
                LocalDate.of(2025, 11, 12), "전시회", "친절한 분");

        SalesContact hanbit = save("이한빛", "Hanbit Lee", "010-2222-3333", "031-2000-3000",
                "hb.lee@hanbittech.kr", "hanbit.private@gmail.com",
                LocalDate.of(2026, 1, 8), "소개", null);

        SalesContact partner = save("박파트너", null, "010-3333-4444", null,
                "partner@starpartners.com", null,
                LocalDate.of(2026, 2, 20), "미팅", null);

        SalesContact mirae = save("최미래", "Mirae Choi", "010-4444-5555", "02-7000-8000",
                "future@mirae.com", null,
                LocalDate.of(2026, 3, 15), "전시회", "결정권자");

        SalesContact huimyeon = save("강휴면", null, "010-5555-6666", null,
                null, "kang.huimyeon@naver.com",
                LocalDate.of(2024, 9, 1), "기타", null);

        log.info("영업 명부 테스트 데이터 5건 생성 완료");

        Long c1 = customerByCode.get("C0001"); // 대성상사
        Long c2 = customerByCode.get("C0002"); // 한빛테크
        Long c3 = customerByCode.get("C0003"); // 스타파트너스
        Long c4 = customerByCode.get("C0004"); // 미래글로벌
        Long c5 = customerByCode.get("C0005"); // 휴면상사

        // 정대성 — 이전 외부 회사 이직 → 현재 대성상사 구매팀장
        if (c1 != null) {
            saveEmployment(daesung.getId(), null, "DS구매솔루션",
                    "구매팀 사원", "구매팀",
                    LocalDate.of(2020, 3, 1), LocalDate.of(2025, 10, 31),
                    DepartureType.JOB_CHANGE, "대성상사 이직");
            saveEmployment(daesung.getId(), c1, null,
                    "구매팀장", "구매팀",
                    LocalDate.of(2025, 11, 1), null, null, null);
        }

        // 이한빛 — 대성상사 사원 이직 → 현재 한빛테크 기획팀장
        if (c2 != null) {
            if (c1 != null) {
                saveEmployment(hanbit.getId(), c1, null,
                        "기획팀 사원", "기획팀",
                        LocalDate.of(2021, 4, 1), LocalDate.of(2025, 12, 31),
                        DepartureType.JOB_CHANGE, "한빛테크 스카우트");
            }
            saveEmployment(hanbit.getId(), c2, null,
                    "기획팀장", "기획팀",
                    LocalDate.of(2026, 1, 2), null, null, null);
        }

        // 박파트너 — 스타파트너스 대표 (현재)
        if (c3 != null) {
            saveEmployment(partner.getId(), c3, null,
                    "대표", null,
                    LocalDate.of(2024, 1, 10), null, null, null);
        }

        // 최미래 — 미래글로벌 사업개발 (현재)
        if (c4 != null) {
            saveEmployment(mirae.getId(), c4, null,
                    "사업개발팀장", "사업개발팀",
                    LocalDate.of(2025, 6, 1), null, null, null);
        }

        // 강휴면 — 휴면상사 영업과장 (퇴직 종료)
        if (c5 != null) {
            saveEmployment(huimyeon.getId(), c5, null,
                    "영업과장", "영업팀",
                    LocalDate.of(2018, 4, 1), LocalDate.of(2024, 8, 31),
                    DepartureType.RETIREMENT, "정년 은퇴");
        }

        log.info("영업 명부 재직 이력 시드 {}건 생성 완료", employmentRepository.count());
    }

    @SuppressWarnings("SameParameterValue")
    private SalesContact save(String name,
                              String nameEn,
                              String mobilePhone,
                              String officePhone,
                              String email,
                              String personalEmail,
                              LocalDate metAt,
                              String metVia,
                              String note) {
        return contactRepository.save(SalesContact.builder()
                .name(name)
                .nameEn(nameEn)
                .mobilePhone(mobilePhone)
                .officePhone(officePhone)
                .email(email)
                .personalEmail(personalEmail)
                .metAt(metAt)
                .metVia(metVia)
                .note(note)
                .build());
    }

    @SuppressWarnings("SameParameterValue")
    private void saveEmployment(Long contactId,
                                Long customerId,
                                String externalCompanyName,
                                String position,
                                String department,
                                LocalDate startDate,
                                LocalDate endDate,
                                DepartureType departureType,
                                String departureNote) {
        employmentRepository.save(SalesContactEmployment.builder()
                .contactId(contactId)
                .customerId(customerId)
                .externalCompanyName(externalCompanyName)
                .position(position)
                .department(department)
                .startDate(startDate)
                .endDate(endDate)
                .departureType(departureType)
                .departureNote(departureNote)
                .build());
    }
}
