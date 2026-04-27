package io.github.ladium1.erp.salescontact.internal.init;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.salescontact.internal.dto.AcquisitionSourceInfo;
import io.github.ladium1.erp.salescontact.internal.entity.DepartureType;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContactEmployment;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContactSource;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactEmploymentRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactSourceRepository;
import io.github.ladium1.erp.salescontact.internal.service.AcquisitionSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 서버 실행 시 영업 명부 + 재직 이력 + 만난 경로 매핑 시드 — 명부가 하나도 없을 때만.
 * 만난 경로 마스터 / 고객사 마스터가 모두 존재해야 매핑이 완성되며, 둘 중 하나라도 없으면 스킵 (재기동 시 재시도).
 */
@Slf4j
@Component
@Order(300) // 의존성: customer (Order=100), acquisitionSource (Order=100). ddl-auto=create 환경에서 매 재기동마다 일관된 시드를 위해 명시.
@RequiredArgsConstructor
public class SalesContactInitializer implements ApplicationRunner {

    private final SalesContactRepository contactRepository;
    private final SalesContactEmploymentRepository employmentRepository;
    private final SalesContactSourceRepository contactSourceRepository;
    private final CustomerApi customerApi;
    private final AcquisitionSourceService acquisitionSourceService;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (contactRepository.count() > 0) {
            return;
        }

        Map<String, Long> sourceByName = acquisitionSourceService.findAll().stream()
                .collect(Collectors.toMap(AcquisitionSourceInfo::name, AcquisitionSourceInfo::id));
        if (sourceByName.isEmpty()) {
            log.warn("컨택 경로 마스터 시드가 없어 영업 명부 테스트 데이터 초기화를 건너뜀 — 재기동 시 다시 시도");
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
                LocalDate.of(2025, 11, 12), "친절한 분");

        SalesContact hanbit = save("이한빛", "Hanbit Lee", "010-2222-3333", "031-2000-3000",
                "hb.lee@hanbittech.kr", "hanbit.private@gmail.com",
                LocalDate.of(2026, 1, 8), null);

        SalesContact partner = save("박파트너", null, "010-3333-4444", null,
                "partner@starpartners.com", null,
                LocalDate.of(2026, 2, 20), null);

        SalesContact mirae = save("최미래", "Mirae Choi", "010-4444-5555", "02-7000-8000",
                "future@mirae.com", null,
                LocalDate.of(2026, 3, 15), "결정권자");

        SalesContact huimyeon = save("강휴면", null, "010-5555-6666", null,
                null, "kang.huimyeon@naver.com",
                LocalDate.of(2024, 9, 1), null);

        SalesContact junjin = save("김준진", "Junjin Kim", "010-6666-7777", "051-3000-4000",
                "jj.kim@busanmach.kr", null,
                LocalDate.of(2024, 5, 22), "BUTECH 부스에서 첫 미팅");

        SalesContact yejin = save("박예진", "Yejin Park", "010-7777-8888", null,
                "yj.park@kdex.kr", "yejin.p@gmail.com",
                LocalDate.of(2024, 9, 18), null);

        SalesContact taehyun = save("이태현", null, "010-8888-9999", "02-5000-6000",
                "th.lee@metalindustry.co.kr", null,
                LocalDate.of(2024, 6, 11), "추천 받은 후 후속 컨택");

        SalesContact bohyun = save("최보현", "Bohyun Choi", "010-9999-1010", null,
                "bh.choi@simtos-followup.kr", null,
                LocalDate.of(2024, 4, 5), null);

        SalesContact sangwoo = save("정상우", "Sangwoo Jung", "010-1010-2020", "02-9000-1000",
                "sw.jung@harf.kr", null,
                LocalDate.of(2024, 8, 30), "항공우주 분야");

        log.info("영업 명부 테스트 데이터 10건 생성 완료");

        // 컨택 경로 매핑 — 한 명을 여러 경로로 받은 케이스 (정대성, 김준진) 포함
        linkSources(daesung.getId(), sourceByName, "SIMTOS26", "김창훈부사장명함");
        linkSources(hanbit.getId(), sourceByName, "이세명대표님명함");
        linkSources(partner.getId(), sourceByName, "납품업체");
        linkSources(mirae.getId(), sourceByName, "DAMEX 2025");
        linkSources(huimyeon.getId(), sourceByName, "인터넷조사");
        linkSources(junjin.getId(), sourceByName, "BUTECH23", "EXBUTECH25");
        linkSources(yejin.getId(), sourceByName, "KADEX24");
        linkSources(taehyun.getId(), sourceByName, "이세명대표님명함", "METAL24");
        linkSources(bohyun.getId(), sourceByName, "SIMTOS24");
        linkSources(sangwoo.getId(), sourceByName, "HARF24");

        log.info("영업 명부 컨택 경로 매핑 {}건 생성 완료", contactSourceRepository.count());

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

        // 신규 5명 — 외부 회사로 재직 정보 (자유 입력 회사명)
        saveEmployment(junjin.getId(), null, "부산기계산업",
                "기술개발팀장", "기술개발팀",
                LocalDate.of(2022, 3, 1), null, null, null);

        saveEmployment(yejin.getId(), null, "K-DEX 솔루션",
                "사업개발 매니저", "사업개발팀",
                LocalDate.of(2023, 6, 15), null, null, null);

        saveEmployment(taehyun.getId(), null, "메탈인더스트리",
                "구매팀 차장", "구매팀",
                LocalDate.of(2021, 9, 1), null, null, null);

        saveEmployment(bohyun.getId(), null, "보현엔지니어링",
                "대표", null,
                LocalDate.of(2020, 1, 1), null, null, null);

        saveEmployment(sangwoo.getId(), null, "한라항공",
                "수석연구원", "R&D센터",
                LocalDate.of(2019, 5, 1), null, null, null);

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
                              String note) {
        return contactRepository.save(SalesContact.builder()
                .name(name)
                .nameEn(nameEn)
                .mobilePhone(mobilePhone)
                .officePhone(officePhone)
                .email(email)
                .personalEmail(personalEmail)
                .metAt(metAt)
                .note(note)
                .build());
    }

    private void linkSources(Long contactId, Map<String, Long> sourceByName, String... names) {
        List<SalesContactSource> rows = java.util.Arrays.stream(names)
                .map(sourceByName::get)
                .filter(java.util.Objects::nonNull)
                .map(sid -> SalesContactSource.builder()
                        .contactId(contactId)
                        .sourceId(sid)
                        .build())
                .toList();
        if (!rows.isEmpty()) {
            contactSourceRepository.saveAll(rows);
        }
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
