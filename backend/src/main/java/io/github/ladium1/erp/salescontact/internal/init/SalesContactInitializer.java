package io.github.ladium1.erp.salescontact.internal.init;

import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 서버 실행 시 영업 명부 테스트 데이터 시드 — 명부가 하나도 없을 때만.
 * 재직 이력은 시드하지 않는다 (외부 모듈 customer 와의 결합을 줄이기 위해).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesContactInitializer implements ApplicationRunner {

    private final SalesContactRepository contactRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (contactRepository.count() > 0) {
            return;
        }

        log.info("영업 명부 테스트 데이터 초기화 시작");

        save("정대성", "Daesung Jung", "010-1111-2222", "02-3000-1000",
                "ds.jung@daesung.co.kr", null,
                LocalDate.of(2025, 11, 12), "전시회", "친절한 분");

        save("이한빛", "Hanbit Lee", "010-2222-3333", "031-2000-3000",
                "hb.lee@hanbittech.kr", "hanbit.private@gmail.com",
                LocalDate.of(2026, 1, 8), "소개", null);

        save("박파트너", null, "010-3333-4444", null,
                "partner@starpartners.com", null,
                LocalDate.of(2026, 2, 20), "미팅", null);

        save("최미래", "Mirae Choi", "010-4444-5555", "02-7000-8000",
                "future@mirae.com", null,
                LocalDate.of(2026, 3, 15), "전시회", "결정권자");

        save("강휴면", null, "010-5555-6666", null,
                null, "kang.huimyeon@naver.com",
                LocalDate.of(2024, 9, 1), "기타", null);

        log.info("영업 명부 테스트 데이터 5건 생성 완료");
    }

    @SuppressWarnings("SameParameterValue")
    private void save(String name,
                      String nameEn,
                      String mobilePhone,
                      String officePhone,
                      String email,
                      String personalEmail,
                      LocalDate metAt,
                      String metVia,
                      String note) {
        contactRepository.save(SalesContact.builder()
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
}
