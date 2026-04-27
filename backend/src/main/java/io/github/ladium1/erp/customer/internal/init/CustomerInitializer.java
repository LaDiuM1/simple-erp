package io.github.ladium1.erp.customer.internal.init;

import io.github.ladium1.erp.customer.internal.entity.Address;
import io.github.ladium1.erp.customer.internal.entity.Customer;
import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import io.github.ladium1.erp.customer.internal.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 서버 실행 시 고객사 테스트 데이터 시드.
 * 고객사가 하나도 없을 때만 분류별 샘플 데이터를 생성한다.
 */
@Slf4j
@Component
@Order(100) // 의존 없음 — 다른 도메인 (영업 명부 등) 시드의 선결 조건. ddl-auto=create 환경에서 결정적 순서 보장.
@RequiredArgsConstructor
public class CustomerInitializer implements ApplicationRunner {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (customerRepository.count() > 0) {
            return;
        }

        log.info("고객사 테스트 데이터 초기화 시작");

        save("C0001", "대성상사", "Daesung Co., Ltd.", "123-45-67890",
                "김대성", "02-1111-2222", "contact@daesung.co.kr",
                "06236", "서울특별시 강남구 테헤란로 123", "5층",
                CustomerType.KEY_ACCOUNT, CustomerStatus.ACTIVE, LocalDate.of(2022, 3, 1));

        save("C0002", "한빛테크", "Hanbit Tech Inc.", "234-56-78901",
                "이한빛", "031-333-4444", "info@hanbittech.kr",
                "13494", "경기도 성남시 분당구 판교로 200", "B동 7층",
                CustomerType.GENERAL, CustomerStatus.ACTIVE, LocalDate.of(2023, 7, 15));

        save("C0003", "스타파트너스", null, "345-67-89012",
                "박파트너", "051-555-6666", "hello@starpartners.com",
                "48058", "부산광역시 해운대구 센텀중앙로 50", "12층",
                CustomerType.PARTNER, CustomerStatus.ACTIVE, LocalDate.of(2024, 1, 10));

        save("C0004", "미래글로벌", "Mirae Global", "456-78-90123",
                "최미래", "02-7777-8888", null,
                "04524", "서울특별시 중구 을지로 100", "10층",
                CustomerType.POTENTIAL, CustomerStatus.ACTIVE, null);

        save("C0005", "휴면상사", null, null,
                "강휴면", null, null,
                null, null, null,
                CustomerType.GENERAL, CustomerStatus.INACTIVE, LocalDate.of(2020, 5, 1));

        log.info("고객사 테스트 데이터 5건 생성 완료");
    }

    @SuppressWarnings("SameParameterValue")
    private void save(String code,
                      String name,
                      String nameEn,
                      String bizRegNo,
                      String representative,
                      String phone,
                      String email,
                      String zipCode,
                      String roadAddress,
                      String detailAddress,
                      CustomerType type,
                      CustomerStatus status,
                      LocalDate tradeStartDate) {
        Address address = (zipCode == null && roadAddress == null && detailAddress == null)
                ? null
                : Address.builder()
                    .zipCode(zipCode)
                    .roadAddress(roadAddress)
                    .detailAddress(detailAddress)
                    .build();

        customerRepository.save(Customer.builder()
                .code(code)
                .name(name)
                .nameEn(nameEn)
                .bizRegNo(bizRegNo)
                .representative(representative)
                .phone(phone)
                .email(email)
                .address(address)
                .type(type)
                .status(status)
                .tradeStartDate(tradeStartDate)
                .build());
    }
}
